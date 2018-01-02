package com.polidea.shuttle.domain.app;

import com.google.common.annotations.VisibleForTesting;
import com.polidea.shuttle.domain.build.Build;
import com.polidea.shuttle.domain.project.Project;
import com.polidea.shuttle.infrastructure.database.BaseEntity;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static javax.persistence.FetchType.LAZY;

@Entity(name = "apps")
public class App extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private Platform platform;

    @NotBlank
    private String appId;

    private String name;

    private String iconHref;

    @OneToMany(fetch = FetchType.LAZY,
               mappedBy = "app",
               cascade = CascadeType.ALL)
    private List<Build> rawBuilds = new LinkedList<>();

    @ManyToOne(fetch = LAZY,
               cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH},
               optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    private boolean isDeleted = false;

    // constructor without parameters is required by Hibernate
    @SuppressWarnings("unused")
    public App() {
    }

    public App(Project project, Platform platform, String appId, String name) {
        this.appId = appId;
        this.platform = platform;
        this.project = project;
        this.name = name;
    }

    public OptionalLong lastBuildDate(boolean userCanViewNotPublished) {
        return userCanViewNotPublished ? lastReleaseDate() : lastPublishedReleaseDate();
    }

    public Optional<Build> lastBuild(boolean userCanViewNotPublished) {
        return userCanViewNotPublished ? lastReleasedBuild() : lastPublishedReleasedBuild();
    }

    public OptionalLong lastPublishedReleaseDate() {
        return builds().stream()
                       .filter(build -> build.isPublished())
                       .mapToLong(build -> build.releaseDate())
                       .max();
    }

    private OptionalLong lastReleaseDate() {
        return builds().stream()
                       .mapToLong(build -> build.releaseDate())
                       .max();
    }

    private Optional<Build> lastReleasedBuild() {
        return builds().stream()
                       .max(Comparator.comparingLong(Build::releaseDate));
    }

    private Optional<Build> lastPublishedReleasedBuild() {
        return builds().stream()
                       .filter(build -> build.isPublished())
                       .max(Comparator.comparingLong(Build::releaseDate));
    }

    public String appId() {
        return appId;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Platform platform() {
        return platform;
    }

    public Project project() {
        return project;
    }

    public String iconHref() {
        return iconHref != null
            ? iconHref
            : project.iconHref();
    }

    public void setIconHref(String iconHref) {
        this.iconHref = iconHref;
    }

    public Set<Build> builds() {
        return rawBuilds.stream()
                        .filter(build -> !build.isDeleted())
                        .collect(toSet());
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void delete() {
        builds().forEach(build -> build.delete());
        isDeleted = true;
    }

    @VisibleForTesting
    public void setRawBuilds(List<Build> rawBuilds) {
        this.rawBuilds = rawBuilds;
    }

}
