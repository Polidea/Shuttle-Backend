package com.polidea.shuttle.domain.app;

import com.polidea.shuttle.error_codes.ErrorCode;
import com.polidea.shuttle.error_codes.NotFoundException;

import static java.lang.String.format;

public class AppNotFoundInProjectException extends NotFoundException {

    public AppNotFoundInProjectException(Platform platform, String appId, Integer projectId) {
        super(
            ErrorCode.APP_NOT_FOUND_IN_PROJECT,
            format("App '%s' for platform '%s' was not found in project of ID '%s'",
                   appId,
                   platform,
                   projectId));
    }
}
