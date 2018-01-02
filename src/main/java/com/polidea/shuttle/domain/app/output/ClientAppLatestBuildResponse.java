package com.polidea.shuttle.domain.app.output;

public class ClientAppLatestBuildResponse {

    public String appId;

    public Long lastReleaseDate;

    public ClientAppLatestBuildResponse(String appId, Long lastReleaseDate) {
        this.appId = appId;
        this.lastReleaseDate = lastReleaseDate;
    }
}
