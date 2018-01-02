package com.polidea.shuttle.domain.build.output;

import com.polidea.shuttle.domain.build.Build;
import com.polidea.shuttle.domain.user.output.ClientBuildPermissionsResponse;

public class ClientIosBuildResponse extends ClientBuildResponse {

    public String prefixSchema;

    public ClientIosBuildResponse(Build build, boolean isFavorite, ClientBuildPermissionsResponse permissions) {
        super(build, isFavorite, permissions);
        this.prefixSchema = build.buildIdentifier();
    }
}
