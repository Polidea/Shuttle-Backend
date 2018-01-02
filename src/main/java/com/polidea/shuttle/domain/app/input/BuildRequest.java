package com.polidea.shuttle.domain.app.input;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.polidea.shuttle.infrastructure.json.BuildRequestDeserializer;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@JsonDeserialize(using = BuildRequestDeserializer.class)
public class BuildRequest {

    // versionCode for Android or prefixSchema for iOS
    @NotNull
    private String buildIdentifier;

    @NotBlank
    private String version;

    private String releaseNotes;

    @NotBlank
    private String href;

    @NotNull
    private Long bytes;

    @NotNull
    private String releaserEmail;

    public BuildRequest(String buildIdentifier, String version, String releaseNotes, String href, Long bytes, String releaserEmail) {
        this.buildIdentifier = buildIdentifier;
        this.version = version;
        this.releaseNotes = releaseNotes;
        this.href = href;
        this.bytes = bytes;
        this.releaserEmail = releaserEmail;
    }

    public BuildRequest() {
    }

    public String getBuildIdentifier() {
        return buildIdentifier;
    }

    public void setBuildIdentifier(String buildIdentifier) {
        this.buildIdentifier = buildIdentifier;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public Long getBytes() {
        return bytes;
    }

    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    public String getReleaserEmail() {
        return releaserEmail;
    }

    public void setReleaserEmail(String releaserEmail) {
        this.releaserEmail = releaserEmail;
    }
}
