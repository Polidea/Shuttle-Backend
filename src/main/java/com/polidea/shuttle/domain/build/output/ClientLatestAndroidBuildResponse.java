package com.polidea.shuttle.domain.build.output;

import com.polidea.shuttle.domain.build.Build;

public class ClientLatestAndroidBuildResponse extends ClientLatestBuildResponse {

    public Long versionCode;

    public ClientLatestAndroidBuildResponse(Build build) {
        super(build);
        this.versionCode = Long.parseLong(build.buildIdentifier());
    }
}
