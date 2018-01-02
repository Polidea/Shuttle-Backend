package com.polidea.shuttle.domain.user.access_token;

import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.infrastructure.security.tokens.RandomTokenGenerator;
import com.polidea.shuttle.infrastructure.time.TimeService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static com.polidea.shuttle.domain.user.access_token.TokenType.CLIENT;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@Transactional
public class AccessTokenService {

    private final static Logger LOGGER = getLogger(AccessTokenService.class);

    private final AccessTokenRepository accessTokenRepository;
    private final TimeService timeService;
    private final RandomTokenGenerator randomAccessTokens;

    @Autowired
    public AccessTokenService(AccessTokenRepository accessTokenRepository, TimeService timeService, RandomTokenGenerator randomAccessTokens) {
        this.accessTokenRepository = accessTokenRepository;
        this.timeService = timeService;
        this.randomAccessTokens = randomAccessTokens;
    }

    public String saveAccessToken(User tokenOwner, String deviceId, TokenType tokenType) {
        String tokenValueToSet = randomAccessTokens.next();
        Instant creationTime = timeService.currentTime();
        return saveAccessToken(tokenOwner, deviceId, tokenType, tokenValueToSet, creationTime);
    }

    public String saveAccessToken(User tokenOwner,
                                  String deviceId,
                                  TokenType tokenType,
                                  String tokenValue,
                                  Instant creationTime) {
        LOGGER.info("Saving new Access Token: owner '{}', deviceId: '{}', type '{}', token {}",
                    tokenOwner.email(), deviceId, tokenType, tokenValue);
        accessTokenRepository.createAccessToken(
            tokenOwner,
            deviceId,
            tokenType,
            tokenValue,
            creationTime
        );
        return tokenValue;
    }

    public AccessToken findTokenMatching(String tokenValue, TokenType tokenType) {
        return accessTokenRepository.findBy(tokenValue, tokenType)
                                    .orElseThrow(() -> new TokenNotFoundException());
    }

    public void deleteBy(User owner, String deviceId, TokenType tokenType) {
        accessTokenRepository.deleteBy(owner, deviceId, tokenType);
    }

    public void removeToken(String tokenValue) {
        accessTokenRepository.delete(tokenValue, CLIENT);
    }
}
