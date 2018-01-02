package com.polidea.shuttle.domain.project.output;

import java.util.List;

public class AdminProjectListResponse {

    public List<AdminProjectResponse> projects;

    public AdminProjectListResponse(List<AdminProjectResponse> projectResponses) {
        this.projects = projectResponses;
    }

}

