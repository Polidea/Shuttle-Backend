package com.polidea.shuttle.domain.app;

import com.polidea.shuttle.error_codes.ErrorCode;
import com.polidea.shuttle.error_codes.NotFoundException;

import static java.lang.String.format;

public class AppNotFoundException extends NotFoundException {

    public AppNotFoundException(Platform platform, String appId) {
        super(ErrorCode.APP_NOT_FOUND, format("App '%s' for platform '%s' was not found", appId, platform));
    }
}
