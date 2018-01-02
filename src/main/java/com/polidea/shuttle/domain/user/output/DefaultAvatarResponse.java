package com.polidea.shuttle.domain.user.output;

import com.polidea.shuttle.infrastructure.avatars.Avatar;

public class DefaultAvatarResponse {

    public final String imageHref;

    public DefaultAvatarResponse(Avatar avatar) {
        this.imageHref = avatar.url();
    }

}
