package com.polidea.shuttle.domain.user.output;

import com.polidea.shuttle.domain.user.User;

public class ClientProfileResponse {

    public final String email;

    public final String name;

    public final String avatarHref;

    public ClientProfileResponse(User user) {
        this.email = user.email();
        this.name = user.name();
        this.avatarHref = user.avatarHref();
    }

    public ClientProfileResponse(String email, String name) {
        this.email = email;
        this.name = name;
        this.avatarHref = null;
    }
}
