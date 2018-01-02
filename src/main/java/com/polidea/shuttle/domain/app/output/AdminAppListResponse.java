package com.polidea.shuttle.domain.app.output;

import java.util.List;

public class AdminAppListResponse {

    public List<AdminAppResponse> apps;

    public AdminAppListResponse(List<AdminAppResponse> appResponses) {
        this.apps = appResponses;
    }

}
