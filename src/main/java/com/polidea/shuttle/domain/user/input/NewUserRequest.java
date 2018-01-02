package com.polidea.shuttle.domain.user.input;

import com.polidea.shuttle.infrastructure.RegularExpressions;
import com.polidea.shuttle.infrastructure.json.NullableOptionalRequestField;
import com.polidea.shuttle.infrastructure.json.OptionalRequestField;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;


public class NewUserRequest {

    @NotBlank
    @Pattern(regexp = RegularExpressions.EMAIL_REGEX, message = "Invalid email.")
    public String email;

    @NotBlank
    public String name;

    public String avatarHref;

    private OptionalRequestField<Boolean> isVisibleForModerator;

    public OptionalRequestField<Boolean> isVisibleForModerator() {
        return isVisibleForModerator;
    }

    @SuppressWarnings("unused")
    public void setIsVisibleForModerator(Boolean isVisibleForModerator) {
        this.isVisibleForModerator = new NullableOptionalRequestField<>(isVisibleForModerator);
    }

}
