package com.polidea.shuttle.domain.shuttle.output;

import com.polidea.shuttle.domain.build.Build;

public class AdminShuttleBuildResponse {

    public final String qrCodeBase64;
    public final String version;
    public final String href;

    public AdminShuttleBuildResponse(String qrCodeBase64,
                                     Build build) {
        this.qrCodeBase64 = qrCodeBase64;
        this.version = build.versionNumber();
        this.href = build.href();
    }

}
