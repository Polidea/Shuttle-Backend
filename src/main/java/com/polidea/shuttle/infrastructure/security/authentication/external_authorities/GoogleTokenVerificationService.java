package com.polidea.shuttle.infrastructure.security.authentication.external_authorities;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class GoogleTokenVerificationService {

    private final static Logger LOGGER = getLogger(GoogleTokenVerificationService.class);

    private final GoogleIdTokenVerifier tokenVerifier;

    @Value("${shuttle.google.project.client.id}")
    private String googleApplicationClientId;

    @Autowired
    public GoogleTokenVerificationService(GoogleIdTokenVerifier tokenVerifier) {
        this.tokenVerifier = tokenVerifier;
    }

    public Optional<String> verifyToken(String tokenValue) {
        if (isBlank(tokenValue)) {
            return Optional.empty();
        }

        GoogleIdToken googleIdToken;
        try {
            Optional<GoogleIdToken> optionalGoogleIdToken = Optional.ofNullable(tokenVerifier.verify(tokenValue));
            if (!optionalGoogleIdToken.isPresent()) {
                LOGGER.error("Google Token is invalid: {}.", tokenValue);
                return Optional.empty();
            }
            googleIdToken = optionalGoogleIdToken.get();
        } catch (IllegalArgumentException | GeneralSecurityException | IOException exception) {
            LOGGER.error("Google Token verification failed", exception);
            return Optional.empty();
        }

        if (!isClientIdValidIn(googleIdToken)) {
            LOGGER.error("Audience (Google application Client Id) is invalid in this Token: {}.", tokenValue);
            return Optional.empty();
        }

        return Optional.ofNullable(googleIdToken.getPayload().getEmail());
    }

    private boolean isClientIdValidIn(GoogleIdToken googleIdToken) {
        return googleApplicationClientId.equals(googleIdToken.getPayload().getAudience());
    }

}
