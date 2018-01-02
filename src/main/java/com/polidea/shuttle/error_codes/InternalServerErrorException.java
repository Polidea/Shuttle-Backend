package com.polidea.shuttle.error_codes;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

public class InternalServerErrorException extends ShuttleException {

    public InternalServerErrorException(String message, ErrorCode errorCode) {
        super(errorCode, message, INTERNAL_SERVER_ERROR);
    }

}
