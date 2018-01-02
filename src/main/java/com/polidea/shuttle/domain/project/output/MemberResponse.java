package com.polidea.shuttle.domain.project.output;

import com.polidea.shuttle.domain.user.User;

public class MemberResponse {

    public String email;

    public String name;

    public String avatarHref;

    public MemberResponse(User member) {
        this.email = member.email();
        this.name = member.name();
        this.avatarHref = member.avatarHref();
    }
}
