package com.polidea.shuttle.domain.app.output;

import com.polidea.shuttle.domain.build.Build;

public class ClientAppIosLatestBuildResponse extends ClientAppLatestBuildResponse {

    public String prefixSchema;

    public ClientAppIosLatestBuildResponse(String id,
                                           Build build) {
        super(id, build.releaseDate());
        this.prefixSchema = build.buildIdentifier();
    }
}
