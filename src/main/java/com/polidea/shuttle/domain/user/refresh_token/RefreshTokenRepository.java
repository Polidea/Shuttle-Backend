package com.polidea.shuttle.domain.user.refresh_token;

import com.polidea.shuttle.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
class RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;

    @Autowired
    RefreshTokenRepository(RefreshTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    void createOrUpdateRefreshToken(User tokenOwner, String deviceId, String value, Instant creationTime) {
        jpaRepository.deleteByOwnerAndDeviceId(tokenOwner, deviceId);

        RefreshToken newRefreshToken = new RefreshToken(
            tokenOwner,
            deviceId,
            value,
            creationTime
        );
        jpaRepository.save(newRefreshToken);
    }

    Optional<RefreshToken> findBy(String tokenValue) {
        return jpaRepository.findByValue(tokenValue);
    }

    void deleteByOwnerAndDeviceId(User owner, String deviceId) {
        jpaRepository.deleteByOwnerAndDeviceId(owner, deviceId);
    }
}
