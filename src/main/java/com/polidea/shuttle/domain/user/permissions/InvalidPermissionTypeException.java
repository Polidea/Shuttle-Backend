package com.polidea.shuttle.domain.user.permissions;

import com.polidea.shuttle.error_codes.BadRequestException;

import static java.lang.String.format;

public class InvalidPermissionTypeException extends BadRequestException {

    public InvalidPermissionTypeException(String permissionTypeName) {
        super(format("Permission '%s' is invalid", permissionTypeName));
    }

}
