package com.polidea.shuttle.domain.user.output;

public class UploadAvatarResponse {

    public final String avatarHref;

    public UploadAvatarResponse(String avatarUrl) {
        avatarHref = avatarUrl;
    }

}
