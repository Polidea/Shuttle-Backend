package com.polidea.shuttle.error_codes;

import org.springframework.http.HttpStatus;

public abstract class ShuttleException extends RuntimeException {

    private final HttpStatus httpStatus;

    private final ErrorCode errorCode;

    public ShuttleException(ErrorCode errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
