package com.polidea.shuttle.domain.build.output;

import com.polidea.shuttle.domain.build.Build;

public class AdminAndroidBuildResponse extends AdminBuildResponse {

    public Long versionCode;

    public AdminAndroidBuildResponse(Build build) {
        super(build);
        this.versionCode = Long.parseLong(build.buildIdentifier());
    }
}
