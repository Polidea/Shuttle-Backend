package com.polidea.shuttle.domain.project;

import com.polidea.shuttle.error_codes.ConflictException;

import static java.lang.String.format;

public class DuplicateProjectException extends ConflictException {

    public DuplicateProjectException(String name) {
        super(format("Project named '%s' already exists.", name));
    }

}
