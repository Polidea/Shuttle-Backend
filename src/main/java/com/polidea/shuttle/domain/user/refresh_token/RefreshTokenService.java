package com.polidea.shuttle.domain.user.refresh_token;

import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.infrastructure.security.tokens.RandomTokenGenerator;
import com.polidea.shuttle.infrastructure.time.TimeService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@Service
@Transactional
public class RefreshTokenService {

    private final static Logger LOGGER = getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final TimeService timeService;
    private final RandomTokenGenerator randomRefreshTokens;

    @Autowired
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               TimeService timeService,
                               RandomTokenGenerator randomRefreshTokens) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.timeService = timeService;
        this.randomRefreshTokens = randomRefreshTokens;
    }

    public String saveRefreshToken(User tokenOwner, String deviceId) {
        String tokenValueToSet = randomRefreshTokens.next();
        Instant creationTime = timeService.currentTime();
        return saveRefreshToken(tokenOwner, deviceId, tokenValueToSet, creationTime);
    }

    public String saveRefreshToken(User tokenOwner,
                                   String deviceId,
                                   String tokenValue,
                                   Instant creationTime) {
        LOGGER.info("Saving new Refresh Token: owner '{}', deviceId: '{}', type '{}', token {}",
                    tokenOwner.email(), deviceId, tokenValue);
        refreshTokenRepository.createOrUpdateRefreshToken(
            tokenOwner,
            deviceId,
            tokenValue,
            creationTime
        );
        return tokenValue;
    }

    public void deleteBy(User tokenOwner, String deviceId) {
        refreshTokenRepository.deleteByOwnerAndDeviceId(tokenOwner, deviceId);
    }

    public Optional<RefreshToken> findRefreshTokenMatching(String tokenValue) {
        return refreshTokenRepository.findBy(tokenValue);
    }
}
