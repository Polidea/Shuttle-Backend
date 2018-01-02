package com.polidea.shuttle.domain.user.login.input;

import org.hibernate.validator.constraints.NotBlank;

public class MobileAuthenticationTokenRequest {

    @NotBlank
    public String deviceId;

    @NotBlank
    public String email;

    @NotBlank
    public String code;

}
