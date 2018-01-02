package com.polidea.shuttle.domain.build;

import com.google.common.annotations.VisibleForTesting;
import com.polidea.shuttle.domain.app.App;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.infrastructure.database.BaseEntity;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import static javax.persistence.FetchType.LAZY;

@Entity(name = "builds")
public class Build extends BaseEntity {

    @NotBlank
    private String buildIdentifier;

    private String versionNumber;

    private String releaseNotes;

    @NotBlank
    private String href;

    @Column(name = "bytes")
    private Long bytesCount;

    @NotNull
    private Long creationTimestamp;

    @NotNull
    private Boolean isPublished;

    private boolean isDeleted = false;

    @ManyToOne(fetch = LAZY,
               cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH},
               optional = false)
    @JoinColumn(name = "app_id", nullable = false)
    private App app;

    @ManyToOne(fetch = LAZY,
               cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "releaser_id")
    private User releaser;

    @Column(name = "releaser_email")
    private String releaserEmail;

    // constructor without parameters is required by Hibernate
    @SuppressWarnings("unused")
    public Build() {
    }

    public Build(String buildIdentifier,
                 String versionNumber,
                 String releaseNotes,
                 String href,
                 Long bytesCount,
                 App app,
                 User releaser,
                 String releaserEmail) {
        this.buildIdentifier = buildIdentifier;
        this.versionNumber = versionNumber;
        this.releaseNotes = releaseNotes;
        this.href = href;
        this.bytesCount = bytesCount;
        this.app = app;
        this.isPublished = false;
        this.releaser = releaser;
        this.releaserEmail = releaserEmail;
        // TODO Use time provided by TimeService
        this.creationTimestamp = System.currentTimeMillis();
    }

    public String buildIdentifier() {
        return buildIdentifier;
    }

    public String versionNumber() {
        return versionNumber;
    }

    public String releaseNotes() {
        return releaseNotes;
    }

    public String releaserEmail() {
        return releaserEmail;
    }

    public String href() {
        return href;
    }

    public Boolean isPublished() {
        return isPublished;
    }

    public void publish() {
        isPublished = true;
    }

    public void unpublish() {
        isPublished = false;
    }

    public Long releaseDate() {
        return creationTimestamp;
    }

    public Long bytesCount() {
        return bytesCount;
    }

    public App app() {
        return app;
    }

    public User releaser() {
        return releaser;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void delete() {
        isDeleted = true;
    }

    @VisibleForTesting
    public void setReleaseDate(long releaseDate) {
        this.creationTimestamp = releaseDate;
    }
}
