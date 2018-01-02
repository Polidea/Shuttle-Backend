package com.polidea.shuttle.domain.build.output;

import com.polidea.shuttle.domain.build.Build;
import com.polidea.shuttle.domain.user.output.ClientBuildPermissionsResponse;

public class ClientAndroidBuildResponse extends ClientBuildResponse {

    public Long versionCode;

    public ClientAndroidBuildResponse(Build build, boolean isFavorite, ClientBuildPermissionsResponse permissions) {
        super(build, isFavorite, permissions);
        this.versionCode = Long.parseLong(build.buildIdentifier());
    }
}
