package com.polidea.shuttle.domain.build.output;

import com.polidea.shuttle.domain.build.Build;
import com.polidea.shuttle.domain.user.output.ClientProfileResponse;

public abstract class AdminBuildResponse {

    public String version;

    public String releaseNotes;

    public String href;

    public Long releaseDate;

    public Boolean isPublished;

    public Long bytes;

    public ClientProfileResponse releaser;

    public AdminBuildResponse(Build build) {
        this.version = build.versionNumber();
        this.releaseDate = build.releaseDate();
        this.releaseNotes = build.releaseNotes();
        this.isPublished = build.isPublished();
        this.href = build.href();
        this.bytes = build.bytesCount();
        if(build.releaser() != null) {
            this.releaser = new ClientProfileResponse(build.releaser());
        } else {
            this.releaser = new ClientProfileResponse(build.releaserEmail(), build.releaserEmail());
        }
    }
}
