package com.polidea.shuttle.domain.shuttle.output;

import com.polidea.shuttle.domain.build.Build;

public abstract class ShuttleBuildResponse {

    public String appId;
    public String version;
    public Long releaseDate;
    public String href;
    public Long bytes;

    public ShuttleBuildResponse(String appId, Build build) {
        this.appId = appId;
        this.version = build.versionNumber();
        this.releaseDate = build.releaseDate();
        this.href = build.href();
        this.bytes = build.bytesCount();
    }

}
