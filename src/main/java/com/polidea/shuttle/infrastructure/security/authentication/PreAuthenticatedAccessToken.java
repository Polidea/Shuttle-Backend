package com.polidea.shuttle.infrastructure.security.authentication;

import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class PreAuthenticatedAccessToken extends PreAuthenticatedAuthenticationToken {

    public PreAuthenticatedAccessToken(String principal) {
        super(principal, null);
    }

    String accessToken() {
        return (String) getPrincipal();
    }

}
