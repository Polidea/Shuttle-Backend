package com.polidea.shuttle.domain.app.input;

import org.hibernate.validator.constraints.NotBlank;

public class ProjectAdditionRequest {

    @NotBlank
    public String name;

    public String iconHref;

}

