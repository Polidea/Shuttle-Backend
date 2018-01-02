package com.polidea.shuttle.domain.authentication;

import com.polidea.shuttle.domain.authentication.output.AuthTokensResponse;
import com.polidea.shuttle.domain.user.UserRepository;
import com.polidea.shuttle.infrastructure.security.authentication.external_authorities.GoogleTokenVerificationService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
public class GoogleLogInService {

    private final TokensService tokensService;

    private final UserRepository userRepository;

    private final GoogleTokenVerificationService googleTokenVerificationService;


    public GoogleLogInService(
        TokensService tokensService,
        UserRepository userRepository,
        GoogleTokenVerificationService googleTokenVerificationService) {

        this.tokensService = tokensService;
        this.userRepository = userRepository;
        this.googleTokenVerificationService = googleTokenVerificationService;
    }

    public AuthTokensResponse logIn(String googleToken) {
        return googleTokenVerificationService.verifyToken(googleToken)
                                             .flatMap(email -> generateTokenForEmailOwner(email))
                                             .orElseThrow(() -> new InvalidGoogleTokenException());
    }

    private Optional<AuthTokensResponse> generateTokenForEmailOwner(String email) {
        return userRepository.findUser(email)
                             .map(user -> tokensService.generateTokens(user));
    }
}
