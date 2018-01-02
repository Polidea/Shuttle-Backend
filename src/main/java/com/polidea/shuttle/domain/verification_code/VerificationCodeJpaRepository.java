package com.polidea.shuttle.domain.verification_code;

import com.polidea.shuttle.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface VerificationCodeJpaRepository extends JpaRepository<VerificationCode, Integer> {

    Optional<VerificationCode> findByDeviceIdAndUser(String deviceId, User user);

}
