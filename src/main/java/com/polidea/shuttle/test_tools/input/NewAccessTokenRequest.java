package com.polidea.shuttle.test_tools.input;

import org.hibernate.validator.constraints.NotBlank;

public class NewAccessTokenRequest {

    @NotBlank
    public String email;

    @NotBlank
    public String deviceId;

    @NotBlank
    public String accessToken;
}
