package com.polidea.shuttle.domain.user.output;

import java.util.List;

public class AdminProjectAssigneeListResponse {

    public final List<AdminProjectAssigneeResponse> users;

    public AdminProjectAssigneeListResponse(List<AdminProjectAssigneeResponse> projectAssigneeResponses) {
        this.users = projectAssigneeResponses;
    }

}

