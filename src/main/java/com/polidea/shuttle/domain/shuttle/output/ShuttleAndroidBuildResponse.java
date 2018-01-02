package com.polidea.shuttle.domain.shuttle.output;

import com.polidea.shuttle.domain.build.Build;

public class ShuttleAndroidBuildResponse extends ShuttleBuildResponse {

    public Long versionCode;

    public ShuttleAndroidBuildResponse(String appId, Build build) {
        super(appId, build);
        this.versionCode = Long.parseLong(build.buildIdentifier());
    }

}
