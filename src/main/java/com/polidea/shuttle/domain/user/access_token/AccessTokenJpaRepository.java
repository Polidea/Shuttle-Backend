package com.polidea.shuttle.domain.user.access_token;

import com.polidea.shuttle.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

interface AccessTokenJpaRepository extends JpaRepository<AccessToken, Integer> {

    Optional<AccessToken> findByValueAndType(String value, TokenType type);

    Set<AccessToken> findByOwner(User owner);

    void deleteByValueAndType(String value, TokenType type);

    void deleteByOwner(User owner);

    void deleteByOwnerAndDeviceIdAndType(User owner, String deviceId, TokenType type);
}
