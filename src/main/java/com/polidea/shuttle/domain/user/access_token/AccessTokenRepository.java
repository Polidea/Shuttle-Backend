package com.polidea.shuttle.domain.user.access_token;

import com.polidea.shuttle.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

@Repository
public class AccessTokenRepository {

    private final AccessTokenJpaRepository jpaRepository;

    @Autowired
    AccessTokenRepository(AccessTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    void createAccessToken(User tokenOwner, String deviceId, TokenType tokenType, String value, Instant creationTime) {
        AccessToken newAccessToken = new AccessToken(
            tokenOwner,
            deviceId,
            value,
            tokenType,
            creationTime
        );
        jpaRepository.save(newAccessToken);
    }

    Optional<AccessToken> findBy(String tokenValue, TokenType tokenType) {
        return jpaRepository.findByValueAndType(tokenValue, tokenType);
    }

    void delete(String tokenValue, TokenType tokenType) {
        jpaRepository.deleteByValueAndType(tokenValue, tokenType);
    }

    public Set<AccessToken> findAccessTokensOwnedBy(User owner) {
        return jpaRepository.findByOwner(owner);
    }

    public void deleteAccessTokensOwnedBy(User owner) {
        jpaRepository.deleteByOwner(owner);
    }

    void deleteBy(User owner, String deviceId, TokenType tokenType) {
        jpaRepository.deleteByOwnerAndDeviceIdAndType(owner, deviceId, tokenType);
    }
}
