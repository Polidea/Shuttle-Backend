package com.polidea.shuttle.domain.user.output;

import com.polidea.shuttle.infrastructure.avatars.Avatar;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class DefaultAvatarsResponse {

    public final List<DefaultAvatarResponse> avatars;

    public DefaultAvatarsResponse(List<Avatar> avatars) {
        this.avatars = avatars.stream()
                              .map(avatar -> new DefaultAvatarResponse(avatar))
                              .collect(toList());
    }

}
