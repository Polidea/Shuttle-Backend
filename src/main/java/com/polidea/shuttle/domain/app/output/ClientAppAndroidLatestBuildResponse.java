package com.polidea.shuttle.domain.app.output;

import com.polidea.shuttle.domain.build.Build;

public class ClientAppAndroidLatestBuildResponse extends ClientAppLatestBuildResponse {

    public Long versionCode;

    public ClientAppAndroidLatestBuildResponse(String appId,
                                               Build build) {
        super(appId, build.releaseDate());
        this.versionCode = Long.parseLong(build.buildIdentifier());
    }
}
