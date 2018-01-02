package com.polidea.shuttle.infrastructure.security.authentication;

import com.polidea.shuttle.domain.user.access_token.AccessToken;
import com.polidea.shuttle.domain.user.access_token.TokenType;
import com.polidea.shuttle.infrastructure.time.TimeService;

import java.time.Instant;

import static java.lang.String.format;
import static java.time.temporal.ChronoUnit.MILLIS;

public class ClientTokenExpirationCheck {

    private final Long expirationPeriod;

    private final TimeService timeService;

    public ClientTokenExpirationCheck(TimeService timeService, Long expirationPeriod) {
        this.timeService = timeService;
        this.expirationPeriod = expirationPeriod;
    }

    boolean hasExpired(AccessToken accessToken) {
        if (!accessToken.isOfType(TokenType.CLIENT)) {
            throw new IllegalArgumentException(format("Only %s tokens are allowed.", TokenType.CLIENT));
        }
        Instant tokenCreationTimestamp = accessToken.creationTimestamp();
        Instant currentTimestamp = timeService.currentTime();
        return MILLIS.between(tokenCreationTimestamp, currentTimestamp) > expirationPeriod
            || tokenCreationTimestamp.isAfter(currentTimestamp);
    }

}
