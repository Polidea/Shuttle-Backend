package com.polidea.shuttle.domain.user.login.input;

import org.hibernate.validator.constraints.NotBlank;

public class MobileAuthenticationRefreshTokenRequest {

    @NotBlank
    public String refreshToken;
}
