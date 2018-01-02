package com.polidea.shuttle.infrastructure.security.authentication;


import com.polidea.shuttle.domain.user.access_token.AccessToken;
import com.polidea.shuttle.domain.user.access_token.AccessTokenService;
import com.polidea.shuttle.domain.user.access_token.TokenNotFoundException;
import com.polidea.shuttle.domain.user.access_token.TokenType;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class ContinuousDeploymentAuthenticationProvider extends ShuttleAuthenticationProvider {

    private static final Logger LOGGER = getLogger(ContinuousDeploymentAuthenticationProvider.class);

    private final AccessTokenService accessTokenService;

    @Autowired
    public ContinuousDeploymentAuthenticationProvider(AccessTokenService accessTokenService) {
        this.accessTokenService = accessTokenService;
    }

    @Override
    public AuthenticatedUser authenticate(String token) {
        LOGGER.info("Verifying token value {}", token);
        AccessToken accessToken;

        try {
            accessToken = accessTokenService.findTokenMatching(token, TokenType.CONTINUOUS_DEPLOYMENT);
        } catch (TokenNotFoundException tokenNotFoundException) {
            LOGGER.info("Not found any {} token for token value: {}", TokenType.CONTINUOUS_DEPLOYMENT, token);
            throw new InvalidTokenException();
        }

        LOGGER.info("Found {} token {} for token value {}", TokenType.CONTINUOUS_DEPLOYMENT, accessToken, token);
        return new AuthenticatedUser(
            accessToken.tokenOwner().email(),
            token,
            accessToken.deviceId().orElse(null),
            false
        );
    }
}
