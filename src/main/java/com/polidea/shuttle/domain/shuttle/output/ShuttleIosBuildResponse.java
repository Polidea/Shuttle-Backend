package com.polidea.shuttle.domain.shuttle.output;

import com.polidea.shuttle.domain.build.Build;

public class ShuttleIosBuildResponse extends ShuttleBuildResponse {

    public String prefixSchema;

    public ShuttleIosBuildResponse(String appId, Build build) {
        super(appId, build);
        this.prefixSchema = build.buildIdentifier();
    }

}
