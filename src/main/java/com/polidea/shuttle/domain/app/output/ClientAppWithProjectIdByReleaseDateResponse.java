package com.polidea.shuttle.domain.app.output;

public class ClientAppWithProjectIdByReleaseDateResponse {
    public Integer projectId;
    public ClientAppByReleaseDateResponse app;

    public ClientAppWithProjectIdByReleaseDateResponse(Integer projectId, ClientAppByReleaseDateResponse app) {
        this.projectId = projectId;
        this.app = app;
    }
}
