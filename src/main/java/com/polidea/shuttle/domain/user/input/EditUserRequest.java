package com.polidea.shuttle.domain.user.input;

import com.polidea.shuttle.infrastructure.json.NonBlankOptionalRequestTextField;
import com.polidea.shuttle.infrastructure.json.NullableOptionalRequestField;
import com.polidea.shuttle.infrastructure.json.OptionalRequestField;

public class EditUserRequest {

    private OptionalRequestField<String> name;

    private OptionalRequestField<String> avatarHref;

    private OptionalRequestField<Boolean> isVisibleForModerator;

    public OptionalRequestField<Boolean> isVisibleForModerator() {
        return isVisibleForModerator;
    }

    @SuppressWarnings("unused")
    public void setIsVisibleForModerator(Boolean isVisibleForModerator) {
        this.isVisibleForModerator = new NullableOptionalRequestField<>(isVisibleForModerator);
    }

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
