package com.polidea.shuttle.domain.user.output;

public class ClientAppPermissionsResponse {

    public final boolean canMute;

    public ClientAppPermissionsResponse(boolean canMute) {
        this.canMute = canMute;
    }
}
