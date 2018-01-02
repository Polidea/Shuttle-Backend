package com.polidea.shuttle.domain.shuttle.output;

public class AdminShuttleAllBuildsResponse {

    public final AdminShuttleBuildsResponse publishedBuilds;
    public final AdminShuttleBuildsResponse allBuilds;

    public AdminShuttleAllBuildsResponse(AdminShuttleBuildsResponse publishedBuilds,
                                         AdminShuttleBuildsResponse allBuilds) {
        this.publishedBuilds = publishedBuilds;
        this.allBuilds = allBuilds;
    }
}
