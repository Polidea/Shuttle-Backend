package com.polidea.shuttle.domain.build.output;

import java.util.List;

public class ClientBuildListResponse {

    public List<? extends ClientBuildResponse> builds;

    public ClientBuildListResponse(List<? extends ClientBuildResponse> buildResponses) {
        this.builds = buildResponses;
    }

}
