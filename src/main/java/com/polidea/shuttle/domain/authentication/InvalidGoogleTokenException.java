package com.polidea.shuttle.domain.authentication;

import com.polidea.shuttle.error_codes.ErrorCode;
import com.polidea.shuttle.error_codes.UnauthorizedException;

public class InvalidGoogleTokenException extends UnauthorizedException {
    public InvalidGoogleTokenException() {
        super(ErrorCode.INVALID_GOOGLE_TOKEN, "Google token is invalid");
    }
}
