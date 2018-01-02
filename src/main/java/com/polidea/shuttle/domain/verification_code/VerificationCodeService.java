package com.polidea.shuttle.domain.verification_code;


import com.polidea.shuttle.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VerificationCodeService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final PasswordEncoder encoder;
    private final RandomVerificationCodes randomVerificationCodes;

    @Autowired
    public VerificationCodeService(VerificationCodeRepository verificationCodeRepository,
                                   PasswordEncoder encoder,
                                   RandomVerificationCodes randomVerificationCodes) {
        this.verificationCodeRepository = verificationCodeRepository;
        this.encoder = encoder;
        this.randomVerificationCodes = randomVerificationCodes;
    }

    public String createRandomVerificationCode(String deviceId, User user) {
        String verificationCodeValue = randomVerificationCodes.next();
        String encodedVerificationCode = encoder.encode(verificationCodeValue);
        verificationCodeRepository.createOrUpdateVerificationCodeWithEmail(deviceId, user.email(), encodedVerificationCode);
        return verificationCodeValue;
    }

    // TODO: This method should be moved from the class to some other service
    public void createOrUpdateVerificationCodeWithValue(String deviceId, String email, String newValue) {
        verificationCodeRepository.createOrUpdateVerificationCodeWithEmail(deviceId, email, encoder.encode(newValue));
    }

    // Needed for tests
    // TODO: This method should be moved from the class to some other service
    public void createOrUpdateVerificationCode(String deviceId, String userEmail, String encodedVerificationCode) {
        verificationCodeRepository.createOrUpdateVerificationCodeWithEmail(deviceId, userEmail, encodedVerificationCode);
    }

    public void verifyCode(User user, String deviceId, String verificationCodeValueToCheck) {
        VerificationCode verificationCode =
            verificationCodeRepository.findByDeviceIdAndUser(deviceId, user)
                                      .orElseThrow(() -> new InvalidVerificationCodeException(verificationCodeValueToCheck, deviceId, user.email()));

        if (matchesVerificationCode(verificationCodeValueToCheck, verificationCode.encodedValue())) {
            verificationCodeRepository.delete(verificationCode);
        } else {
            throw new InvalidVerificationCodeException(
                verificationCodeValueToCheck,
                verificationCode.deviceId(),
                user.email()
            );
        }

    }

    private boolean matchesVerificationCode(String codeA, String codeB) {
        return encoder.matches(codeA, codeB);
    }

}
