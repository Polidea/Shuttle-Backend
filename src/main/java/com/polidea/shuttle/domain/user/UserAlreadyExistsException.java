package com.polidea.shuttle.domain.user;

import com.polidea.shuttle.error_codes.ConflictException;
import com.polidea.shuttle.error_codes.ErrorCode;

import static java.lang.String.format;

public class UserAlreadyExistsException extends ConflictException {

    public UserAlreadyExistsException(String email) {
        super(ErrorCode.USER_ALREADY_EXISTS, format("User with e-mail '%s' already exists", email));
    }

}
