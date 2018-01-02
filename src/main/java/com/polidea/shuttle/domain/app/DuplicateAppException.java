package com.polidea.shuttle.domain.app;

import com.polidea.shuttle.error_codes.ConflictException;
import com.polidea.shuttle.error_codes.ErrorCode;

import static java.lang.String.format;

public class DuplicateAppException extends ConflictException {

    public DuplicateAppException(String appId, Platform platform) {
        super(ErrorCode.APP_ALREADY_EXISTS,
              format("App for platform '%s' with appId '%s' already exists", platform, appId));
    }

}
