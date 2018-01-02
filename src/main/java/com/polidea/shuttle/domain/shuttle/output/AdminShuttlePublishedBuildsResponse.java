package com.polidea.shuttle.domain.shuttle.output;

public class AdminShuttlePublishedBuildsResponse {

    public final AdminShuttleBuildsResponse publishedBuilds;

    public AdminShuttlePublishedBuildsResponse(AdminShuttleBuildsResponse publishedBuilds) {
        this.publishedBuilds = publishedBuilds;
    }
}
