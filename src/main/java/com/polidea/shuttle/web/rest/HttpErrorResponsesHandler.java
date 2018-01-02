package com.polidea.shuttle.web.rest;

import com.polidea.shuttle.error_codes.ShuttleException;
import com.polidea.shuttle.infrastructure.mail.UnableToGenerateQRCodeException;
import org.slf4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;

import static com.polidea.shuttle.error_codes.ErrorResponseBody.prepareErrorResponseBody;
import static com.polidea.shuttle.infrastructure.Logging.logException;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@SuppressWarnings("unused")
@ControllerAdvice
public class HttpErrorResponsesHandler {

    private final Logger LOGGER = getLogger(HttpErrorResponsesHandler.class);

    @ExceptionHandler(UnableToGenerateQRCodeException.class)
    public ResponseEntity unableToGenerateQRCode(HttpServletRequest request, UnableToGenerateQRCodeException exception) {
        logException(LOGGER, exception);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(prepareErrorResponseBody("Could not generate QR code", exception, CONFLICT, request));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity conflict(HttpServletRequest request, DataIntegrityViolationException exception) {
        logException(LOGGER, exception);
        return ResponseEntity
            .status(CONFLICT)
            .body(prepareErrorResponseBody("Could not save data", exception, CONFLICT, request));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity notFound(HttpServletRequest request, EntityNotFoundException exception) {
        logException(LOGGER, exception);
        return ResponseEntity
            .status(NOT_FOUND)
            .body(prepareErrorResponseBody("Data not found", exception, NOT_FOUND, request));
    }

    @ExceptionHandler(ShuttleException.class)
    public ResponseEntity notFound(HttpServletRequest request, ShuttleException exception) {
        logException(LOGGER, exception);
        return ResponseEntity
            .status(exception.getHttpStatus())
            .body(prepareErrorResponseBody(exception, request));
    }
}


