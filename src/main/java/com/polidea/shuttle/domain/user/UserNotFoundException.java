package com.polidea.shuttle.domain.user;

import com.polidea.shuttle.error_codes.ErrorCode;
import com.polidea.shuttle.error_codes.NotFoundException;

import static java.lang.String.format;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(String email) {
        super(ErrorCode.USER_NOT_FOUND, format("User '%s' does not exist", email));
    }
}
