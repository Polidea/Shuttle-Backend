package com.polidea.shuttle.domain.shuttle.output;

public class AdminShuttleBuildsResponse {

    public final AdminShuttleBuildResponse ios;
    public final AdminShuttleBuildResponse android;

    public AdminShuttleBuildsResponse(AdminShuttleBuildResponse android,
                                      AdminShuttleBuildResponse ios) {
        this.ios = ios;
        this.android = android;
    }
}
