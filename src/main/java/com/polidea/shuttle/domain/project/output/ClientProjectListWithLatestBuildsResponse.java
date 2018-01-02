package com.polidea.shuttle.domain.project.output;

import java.util.List;

public class ClientProjectListWithLatestBuildsResponse {

    public List<ClientProjectWithLatestBuildsResponse> projects;

    public ClientProjectListWithLatestBuildsResponse(List<ClientProjectWithLatestBuildsResponse> projects) {
        this.projects = projects;
    }
}
