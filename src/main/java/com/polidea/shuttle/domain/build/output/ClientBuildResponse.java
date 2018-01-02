package com.polidea.shuttle.domain.build.output;

import com.polidea.shuttle.domain.build.Build;
import com.polidea.shuttle.domain.user.output.ClientBuildPermissionsResponse;
import com.polidea.shuttle.domain.user.output.ClientProfileResponse;

public abstract class ClientBuildResponse {

    public String version;

    public String releaseNotes;

    public String href;

    public Long releaseDate;

    public Boolean isPublished;

    public Long bytes;

    public boolean isFavorite;

    public ClientProfileResponse releaser;

    public ClientBuildPermissionsResponse permissions;

    ClientBuildResponse(Build build, boolean isFavorite, ClientBuildPermissionsResponse permissions) {
        this.version = build.versionNumber();
        this.releaseDate = build.releaseDate();
        this.releaseNotes = build.releaseNotes();
        this.isPublished = build.isPublished();
        this.href = build.href();
        this.bytes = build.bytesCount();
        this.isFavorite = isFavorite;
        this.permissions = permissions;
        if(build.releaser() != null) {
            this.releaser = new ClientProfileResponse(build.releaser());
        } else {
            this.releaser = new ClientProfileResponse(build.releaserEmail(), build.releaserEmail());
        }
    }
}
