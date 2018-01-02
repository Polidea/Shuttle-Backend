package com.polidea.shuttle.domain.app;

import com.polidea.shuttle.error_codes.BadRequestException;

import static java.lang.String.format;

public class InvalidPlatformException extends BadRequestException {

    public InvalidPlatformException(String platform) {
        super(format("Platform '%s' is invalid", platform));
    }

}
