package com.polidea.shuttle.error_codes;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class UnauthorizedException extends ShuttleException {

    public UnauthorizedException(String message) {
        this(ErrorCode.NOT_DEFINED, message);
    }

    public UnauthorizedException(ErrorCode errorCode, String message) {
        super(errorCode, message, UNAUTHORIZED);
    }
}
