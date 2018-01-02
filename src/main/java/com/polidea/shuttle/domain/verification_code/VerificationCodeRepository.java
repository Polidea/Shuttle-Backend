package com.polidea.shuttle.domain.verification_code;

import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.UserNotFoundException;
import com.polidea.shuttle.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class VerificationCodeRepository {

    private final VerificationCodeJpaRepository jpaRepository;
    private final UserRepository userRepository;

    @Autowired
    public VerificationCodeRepository(VerificationCodeJpaRepository jpaRepository,
                                      UserRepository userRepository) {
        this.jpaRepository = jpaRepository;
        this.userRepository = userRepository;
    }

    public void createOrUpdateVerificationCodeWithEmail(String deviceId, String userEmail, String encodedVerificationCode) {
        User user = userRepository.findUser(userEmail).orElseThrow(() -> new UserNotFoundException(userEmail));

        VerificationCode verificationCode = findByDeviceIdAndUser(deviceId, user)
            .map(code -> updateVerificationCode(code, user, encodedVerificationCode))
            .orElseGet(() -> new VerificationCode(deviceId, user, encodedVerificationCode));

        jpaRepository.save(verificationCode);
    }

    private VerificationCode updateVerificationCode(VerificationCode code, User user, String newCodeValue) {
        code.setUser(user);
        code.setEncodedValue(newCodeValue);
        return code;
    }

    public Optional<VerificationCode> findByDeviceIdAndUser(String deviceId, User user) {
        return jpaRepository.findByDeviceIdAndUser(deviceId, user);
    }

    public void delete(VerificationCode code) {
        jpaRepository.delete(code);
    }
}
