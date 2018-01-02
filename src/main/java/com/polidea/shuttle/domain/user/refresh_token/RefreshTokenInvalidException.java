package com.polidea.shuttle.domain.user.refresh_token;

import com.polidea.shuttle.error_codes.ErrorCode;
import com.polidea.shuttle.error_codes.UnauthorizedException;

public class RefreshTokenInvalidException extends UnauthorizedException {

    public RefreshTokenInvalidException() {
        super(
            ErrorCode.INVALID_REFRESH_TOKEN,
            "RefreshToken is invalid"
        );
    }
}
