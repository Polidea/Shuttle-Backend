package com.polidea.shuttle.domain.project.output;

import java.util.List;

public class ClientArchivedProjectListResponse {

    public List<ClientArchivedProjectResponse> projects;

    public ClientArchivedProjectListResponse(List<ClientArchivedProjectResponse> projectResponses) {
        this.projects = projectResponses;
    }

}

