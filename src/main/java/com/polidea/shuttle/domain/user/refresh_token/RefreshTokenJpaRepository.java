package com.polidea.shuttle.domain.user.refresh_token;

import com.polidea.shuttle.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByValue(String value);

    void deleteByOwnerAndDeviceId(User owner, String deviceId);
}
