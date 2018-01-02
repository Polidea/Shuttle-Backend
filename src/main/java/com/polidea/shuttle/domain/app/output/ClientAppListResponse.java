package com.polidea.shuttle.domain.app.output;

import java.util.List;

public class ClientAppListResponse {

    public List<ClientAppResponse> apps;

    public ClientAppListResponse(List<ClientAppResponse> appResponses) {
        this.apps = appResponses;
    }

}
