package com.polidea.shuttle.domain.user.login.input;

import org.hibernate.validator.constraints.NotBlank;

public class MobileAuthenticationCodeRequest {

    @NotBlank
    public String deviceId;

    @NotBlank
    public String email;

}
