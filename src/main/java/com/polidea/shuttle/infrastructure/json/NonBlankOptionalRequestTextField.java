package com.polidea.shuttle.infrastructure.json;

import com.polidea.shuttle.error_codes.BadRequestException;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class NonBlankOptionalRequestTextField implements OptionalRequestField<String> {

    private final String value;

    public NonBlankOptionalRequestTextField(String value) {
        if (isBlank(value)) {
            throw new BadRequestException("Project Name cannot be empty");
        }
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }

}
