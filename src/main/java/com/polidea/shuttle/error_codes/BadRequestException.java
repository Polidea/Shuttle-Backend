package com.polidea.shuttle.error_codes;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class BadRequestException extends ShuttleException {

    public BadRequestException(String message) {
        this(message, ErrorCode.NOT_DEFINED);
    }

    public BadRequestException(String message, ErrorCode errorCode) {
        super(errorCode, message, BAD_REQUEST);
    }

}
