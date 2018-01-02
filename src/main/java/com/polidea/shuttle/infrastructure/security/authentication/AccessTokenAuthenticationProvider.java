package com.polidea.shuttle.infrastructure.security.authentication;

import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.UserNotFoundException;
import com.polidea.shuttle.domain.user.UserRepository;
import com.polidea.shuttle.domain.user.access_token.AccessToken;
import com.polidea.shuttle.domain.user.access_token.AccessTokenService;
import com.polidea.shuttle.domain.user.access_token.TokenNotFoundException;
import com.polidea.shuttle.domain.user.access_token.TokenType;
import com.polidea.shuttle.domain.user.permissions.PermissionType;
import com.polidea.shuttle.domain.user.permissions.global.GlobalPermissionRepository;
import org.slf4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class AccessTokenAuthenticationProvider extends ShuttleAuthenticationProvider {

    private static final Logger LOGGER = getLogger(AccessTokenAuthenticationProvider.class);

    private final AccessTokenService accessTokenService;
    private final UserRepository userRepository;
    private final GlobalPermissionRepository globalPermissionRepository;
    private final ClientTokenExpirationCheck clientTokenExpirationCheck;

    public AccessTokenAuthenticationProvider(AccessTokenService accessTokenService,
                                             UserRepository userRepository,
                                             GlobalPermissionRepository globalPermissionRepository,
                                             ClientTokenExpirationCheck clientTokenExpirationCheck) {
        this.accessTokenService = accessTokenService;
        this.userRepository = userRepository;
        this.globalPermissionRepository = globalPermissionRepository;
        this.clientTokenExpirationCheck = clientTokenExpirationCheck;
    }

    @Override
    public AuthenticatedUser authenticate(String token) throws AuthenticationException {
        AccessToken accessToken;
        try {
            accessToken = accessTokenService.findTokenMatching(token, TokenType.CLIENT);
        } catch (TokenNotFoundException tokenNotFoundException) {
            LOGGER.info("Not found any {} token for token value: {}", TokenType.CLIENT, token);
            throw new InvalidTokenException();
        }

        if (clientTokenExpirationCheck.hasExpired(accessToken)) {
            LOGGER.info("Found {} token {} for token value {} but it has expired", TokenType.CLIENT, accessToken, token);
            throw new InvalidTokenException();
        }

        String userEmail = accessToken.tokenOwner().email();

        User user = userRepository.findUser(userEmail)
                                  .orElseThrow(() -> new UserNotFoundException(userEmail));

        boolean isGlobalAdmin = globalPermissionRepository.findFor(user)
                                                          .stream()
                                                          .anyMatch(p -> p.isOfType(PermissionType.ADMIN));

        LOGGER.info("Found {} token {} for token value {}", TokenType.CLIENT, accessToken, token);


        return new AuthenticatedUser(
            userEmail,
            token,
            accessToken.deviceId().orElse(null),
            isGlobalAdmin
        );
    }
}
