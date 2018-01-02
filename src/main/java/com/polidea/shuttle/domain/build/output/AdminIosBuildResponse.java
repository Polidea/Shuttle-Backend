package com.polidea.shuttle.domain.build.output;

import com.polidea.shuttle.domain.build.Build;

public class AdminIosBuildResponse extends AdminBuildResponse {

    public String prefixSchema;

    public AdminIosBuildResponse(Build build) {
        super(build);
        this.prefixSchema = build.buildIdentifier();
    }
}
