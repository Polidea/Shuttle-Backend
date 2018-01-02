package com.polidea.shuttle.domain.build;

import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.error_codes.ErrorCode;
import com.polidea.shuttle.error_codes.ForbiddenException;

import static java.lang.String.format;

public class NoPermissionToFetchBuildsException extends ForbiddenException {

    public NoPermissionToFetchBuildsException(Platform platform, String appId) {
        super(
            ErrorCode.NOT_DEFINED,
            format("You do not have permissions to fetch list of builds for appId '%s' on platform '%s'", appId, platform)
        );
    }

}
