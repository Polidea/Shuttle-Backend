package com.polidea.shuttle.domain.project;

import com.polidea.shuttle.error_codes.ErrorCode;
import com.polidea.shuttle.error_codes.NotFoundException;

public class ProjectNotFoundException extends NotFoundException {

    public ProjectNotFoundException() {
        super(ErrorCode.PROJECT_NOT_FOUND, "Project not found");
    }
}
