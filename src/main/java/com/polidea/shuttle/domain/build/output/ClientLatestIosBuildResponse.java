package com.polidea.shuttle.domain.build.output;

import com.polidea.shuttle.domain.build.Build;

public class ClientLatestIosBuildResponse extends ClientLatestBuildResponse {

    public String prefixSchema;

    public ClientLatestIosBuildResponse(Build build) {
        super(build);
        this.prefixSchema = build.buildIdentifier();
    }
}
