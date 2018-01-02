package com.polidea.shuttle.domain.user.output;

public class ClientBuildPermissionsResponse {

    public final boolean canPublish;

    public ClientBuildPermissionsResponse(boolean canPublish) {
        this.canPublish = canPublish;
    }
}
