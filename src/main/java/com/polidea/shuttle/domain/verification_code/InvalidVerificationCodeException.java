package com.polidea.shuttle.domain.verification_code;

import com.polidea.shuttle.error_codes.ErrorCode;
import com.polidea.shuttle.error_codes.UnauthorizedException;

import static java.lang.String.format;

public class InvalidVerificationCodeException extends UnauthorizedException {

    public InvalidVerificationCodeException(String verificationCodeValue, String deviceId, String userEmail) {
        super(
            ErrorCode.INVALID_VERIFICATION_CODE,
            format(
                "Verification Code '%s' is invalid for device '%s' and user '%s'",
                verificationCodeValue,
                deviceId,
                userEmail
            )
        );
    }
}
