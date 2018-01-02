package com.polidea.shuttle.domain.user.input;

import com.polidea.shuttle.infrastructure.json.NonBlankOptionalRequestTextField;
import com.polidea.shuttle.infrastructure.json.NullableOptionalRequestField;
import com.polidea.shuttle.infrastructure.json.OptionalRequestField;

public class ProfileUpdateRequest {

    private OptionalRequestField<String> name;

    private OptionalRequestField<String> avatarHref;

    public void setName(String name) {
        this.name = new NonBlankOptionalRequestTextField(name);
    }

    public OptionalRequestField<String> name() {
        return name;
    }

    public void setAvatarHref(String avatarHref) {
        this.avatarHref = new NullableOptionalRequestField<>(avatarHref);
    }

    public OptionalRequestField<String> avatarHref() {
        return avatarHref;
    }
}
