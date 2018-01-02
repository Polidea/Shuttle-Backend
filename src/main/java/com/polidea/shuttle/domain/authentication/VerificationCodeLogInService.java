package com.polidea.shuttle.domain.authentication;

import com.polidea.shuttle.domain.authentication.output.AuthTokensResponse;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.UserNotFoundException;
import com.polidea.shuttle.domain.user.UserService;
import com.polidea.shuttle.domain.verification_code.InvalidVerificationCodeException;
import com.polidea.shuttle.domain.verification_code.VerificationCodeService;
import com.polidea.shuttle.infrastructure.mail.MailAuthService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import static com.google.api.client.repackaged.com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Service
@Transactional
public class VerificationCodeLogInService {

    private final UserService userService;

    private final VerificationCodeService verificationCodeService;

    private final MailAuthService mailAuthService;

    private final TokensService tokensService;

    public VerificationCodeLogInService(
        UserService userService,
        VerificationCodeService verificationCodeService,
        MailAuthService mailAuthService,
        TokensService tokensService) {

        this.userService = userService;
        this.verificationCodeService = verificationCodeService;
        this.mailAuthService = mailAuthService;
        this.tokensService = tokensService;
    }

    public void sendNewCodeToEmail(String deviceId, String email) {
        User user = userService.findUser(email);
        String verificationCode = verificationCodeService.createRandomVerificationCode(deviceId, user);
        mailAuthService.sendVerificationCode(user.email(), verificationCode);
    }

    public AuthTokensResponse authenticateByCode(String deviceId, String email, String verificationCode) {
        checkArgument(isNotBlank(deviceId), "Device ID must not be null");
        checkArgument(isNotBlank(verificationCode), "Verification code must not be null");

        User user = findCodeOwner(deviceId, email, verificationCode);
        verificationCodeService.verifyCode(user, deviceId, verificationCode);
        return tokensService.generateTokens(user, deviceId);
    }

    private User findCodeOwner(String deviceId, String email, String verificationCode) {
        try {
            return userService.findUser(email);
        } catch (UserNotFoundException userNotFoundException) {
            throw new InvalidVerificationCodeException(verificationCode, deviceId, email);
        }
    }
}
