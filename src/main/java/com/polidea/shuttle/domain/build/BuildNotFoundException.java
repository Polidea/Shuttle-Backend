package com.polidea.shuttle.domain.build;

import com.polidea.shuttle.error_codes.ErrorCode;
import com.polidea.shuttle.error_codes.NotFoundException;

import static java.lang.String.format;

public class BuildNotFoundException extends NotFoundException {

    public BuildNotFoundException() {
        super(ErrorCode.BUILD_NOT_FOUND, "Build not found");
    }

    public BuildNotFoundException(String buildIdentifier) {
        super(ErrorCode.BUILD_NOT_FOUND, format("Build with identifier '%s' was not found", buildIdentifier));
    }
}
