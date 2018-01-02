package com.polidea.shuttle.error_codes;

import static org.springframework.http.HttpStatus.CONFLICT;

public class ConflictException extends ShuttleException {

    public ConflictException(ErrorCode errorCode, String message) {
        super(errorCode, message, CONFLICT);
    }

    public ConflictException(String message) {
        this(ErrorCode.NOT_DEFINED, message);
    }

}
