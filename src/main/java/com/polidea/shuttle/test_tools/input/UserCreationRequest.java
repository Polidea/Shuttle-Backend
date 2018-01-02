package com.polidea.shuttle.test_tools.input;

import com.polidea.shuttle.infrastructure.RegularExpressions;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;

public class UserCreationRequest {

    @NotBlank
    @Pattern(regexp = RegularExpressions.EMAIL_REGEX, message = "Invalid email.")
    public String email;

    @NotBlank
    public String name;
}
