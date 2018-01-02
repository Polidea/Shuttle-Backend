package com.polidea.shuttle.domain.app.output;

import java.util.List;

public class ClientAppListByReleaseDateResponse {
    public List<ClientAppWithProjectIdByReleaseDateResponse> appsByLastReleaseDate;

    public ClientAppListByReleaseDateResponse(List<ClientAppWithProjectIdByReleaseDateResponse> appsByLastReleaseDate) {
        this.appsByLastReleaseDate = appsByLastReleaseDate;
    }
}
