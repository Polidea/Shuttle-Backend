package com.polidea.shuttle.domain.app.input;

import org.hibernate.validator.constraints.NotBlank;

public class PushTokenRequest {

    @NotBlank
    public String token;

}
