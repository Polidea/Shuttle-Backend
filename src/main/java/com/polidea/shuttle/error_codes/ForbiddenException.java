package com.polidea.shuttle.error_codes;

import static org.springframework.http.HttpStatus.FORBIDDEN;

public class ForbiddenException extends ShuttleException {

    public ForbiddenException(String message) {
        super(ErrorCode.NOT_DEFINED, message, FORBIDDEN);
    }

    protected ForbiddenException(ErrorCode errorCode, String message) {
        super(errorCode, message, FORBIDDEN);
    }
}
