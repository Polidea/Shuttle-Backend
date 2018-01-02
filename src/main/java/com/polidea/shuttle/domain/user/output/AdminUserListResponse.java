package com.polidea.shuttle.domain.user.output;

import java.util.List;

public class AdminUserListResponse {

    public final List<AdminUserResponse> users;

    public AdminUserListResponse(List<AdminUserResponse> userResponses) {
        this.users = userResponses;
    }

}
