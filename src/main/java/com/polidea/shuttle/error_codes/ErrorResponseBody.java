package com.polidea.shuttle.error_codes;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.lang.System.currentTimeMillis;

public class ErrorResponseBody {

    public Long timestamp;

    public Integer status;

    public String error;

    @JsonInclude(value = NON_NULL)
    public Integer code;

    public String message;

    public String path;

    public static ErrorResponseBody prepareErrorResponseBody(ShuttleException shuttleException,
                                                             HttpServletRequest request) {
        return new ErrorResponseBody(
            shuttleException.getHttpStatus().getReasonPhrase(),
            shuttleException,
            shuttleException.getHttpStatus(),
            shuttleException.getErrorCode(),
            request
        );
    }

    public static ErrorResponseBody prepareErrorResponseBody(String errorName,
                                                             Exception exception,
                                                             HttpStatus httpStatus,
                                                             HttpServletRequest request) {
        return new ErrorResponseBody(
            errorName,
            exception,
            httpStatus,
            ErrorCode.NOT_DEFINED,
            request
        );
    }

    private ErrorResponseBody(String errorName, Exception exception, HttpStatus httpStatus, ErrorCode errorCode, HttpServletRequest request) {
        this.timestamp = currentTimeMillis();
        this.status = httpStatus.value();
        this.error = errorName;
        this.code = errorCode.value;
        this.message = exception.getMessage();
        this.path = request.getServletPath();
    }

}
