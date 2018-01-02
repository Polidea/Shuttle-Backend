package com.polidea.shuttle.domain.user.output;

public class ClientProjectPermissionsResponse {

    public final boolean canArchive;
    public final boolean canMute;

    public ClientProjectPermissionsResponse(boolean canArchive, boolean canMute) {
        this.canArchive = canArchive;
        this.canMute = canMute;
    }
}
