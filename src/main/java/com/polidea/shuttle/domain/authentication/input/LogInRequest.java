package com.polidea.shuttle.domain.authentication.input;

import org.hibernate.validator.constraints.NotBlank;

public class LogInRequest {

    @NotBlank
    public String token;
}
