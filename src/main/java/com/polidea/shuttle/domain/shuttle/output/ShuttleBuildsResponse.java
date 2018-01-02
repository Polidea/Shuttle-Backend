package com.polidea.shuttle.domain.shuttle.output;

public class ShuttleBuildsResponse {

    public ShuttleBuildResponse latestPublished;

    public ShuttleBuildsResponse(ShuttleBuildResponse latestPublished) {
        this.latestPublished = latestPublished;
    }

}
