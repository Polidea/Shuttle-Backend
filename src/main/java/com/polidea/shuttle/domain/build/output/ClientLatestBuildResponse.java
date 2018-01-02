package com.polidea.shuttle.domain.build.output;

import com.polidea.shuttle.domain.build.Build;

public class ClientLatestBuildResponse {

    public String version;

    public String href;

    public Long releaseDate;

    public Long bytes;

    ClientLatestBuildResponse(Build build) {
        this.version = build.versionNumber();
        this.releaseDate = build.releaseDate();
        this.href = build.href();
        this.bytes = build.bytesCount();
    }
}
