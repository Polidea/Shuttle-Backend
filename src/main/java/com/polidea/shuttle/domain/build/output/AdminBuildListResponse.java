package com.polidea.shuttle.domain.build.output;

import java.util.List;

public class AdminBuildListResponse {

    public List<? extends AdminBuildResponse> builds;

    public AdminBuildListResponse(List<? extends AdminBuildResponse> buildResponses) {
        this.builds = buildResponses;
    }

}
