package com.polidea.shuttle.test_tools.input;

import org.hibernate.validator.constraints.NotBlank;

public class UserDeletionRequest {

    @NotBlank
    public String email;
}
