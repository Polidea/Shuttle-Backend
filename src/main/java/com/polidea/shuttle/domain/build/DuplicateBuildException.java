package com.polidea.shuttle.domain.build;

import com.polidea.shuttle.error_codes.ConflictException;
import com.polidea.shuttle.error_codes.ErrorCode;

import static java.lang.String.format;

public class DuplicateBuildException extends ConflictException {

    public DuplicateBuildException(String buildIdentifier, String versionNumber) {
        super(ErrorCode.BUILD_ALREADY_EXISTS,
              format("Build with identifier '%s' in version '%s' already exists",
                     buildIdentifier,
                     versionNumber)
        );
    }

}
