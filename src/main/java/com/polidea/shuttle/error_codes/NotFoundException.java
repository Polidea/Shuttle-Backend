package com.polidea.shuttle.error_codes;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class NotFoundException extends ShuttleException {

    public NotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message, NOT_FOUND);
    }
}
