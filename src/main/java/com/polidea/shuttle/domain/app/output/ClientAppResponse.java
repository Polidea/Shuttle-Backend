package com.polidea.shuttle.domain.app.output;

import com.polidea.shuttle.domain.app.App;
import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.build.output.ClientLatestBuildResponse;
import com.polidea.shuttle.domain.user.output.ClientAppPermissionsResponse;

public class ClientAppResponse {

    public String id;

    public String name;

    public Platform type;

    public String iconHref;

    public Boolean isMuted;

    public Long lastReleaseDate;

    public ClientAppPermissionsResponse permissions;

    public ClientLatestBuildResponse latestBuild;

    public ClientAppResponse(App app, ClientLatestBuildResponse latestBuild,
                             Boolean isMuted,
                             ClientAppPermissionsResponse permissions,
                             Long lastReleaseDate) {
        this.id = app.appId();
        this.name = app.name();
        this.type = app.platform();
        this.iconHref = app.iconHref();
        this.isMuted = isMuted;
        this.permissions = permissions;
        this.latestBuild = latestBuild;
        this.lastReleaseDate = lastReleaseDate;
    }

}
