package com.polidea.shuttle.domain.app.input;

import com.polidea.shuttle.infrastructure.json.NonBlankOptionalRequestTextField;
import com.polidea.shuttle.infrastructure.json.NullableOptionalRequestField;
import com.polidea.shuttle.infrastructure.json.OptionalRequestField;

public class ProjectEditionRequest {

    private OptionalRequestField<String> name;

    private OptionalRequestField<String> iconHref;

    public void setName(String name) {
        this.name = new NonBlankOptionalRequestTextField(name);
    }

    public OptionalRequestField<String> name() {
        return name;
    }

    public void setIconHref(String iconHref) {
        this.iconHref = new NullableOptionalRequestField<>(iconHref);
    }

    public OptionalRequestField<String> iconHref() {
        return iconHref;
    }
}
