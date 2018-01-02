package com.polidea.shuttle.infrastructure.security.authentication;

import org.springframework.security.core.AuthenticationException;

class InvalidTokenException extends AuthenticationException {

    InvalidTokenException() {
        super("Access Token is invalid");
    }
}
