package com.polidea.shuttle.domain.user;

import com.polidea.shuttle.domain.app.App;
import com.polidea.shuttle.domain.build.Build;
import com.polidea.shuttle.domain.project.Project;
import com.polidea.shuttle.infrastructure.database.BaseEntity;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Entity(name = "store_users")
public class User extends BaseEntity {

    @NotBlank
    private String email;

    private String name;

    private String avatarHref;

    private boolean isVisibleForModerator;

    @ManyToMany
    @JoinTable(name = "builds_favorited_by_users",
               joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
               inverseJoinColumns = @JoinColumn(name = "favorite_build_id", referencedColumnName = "id"))
    private List<Build> rawFavoriteBuilds = new LinkedList<>();

    @ManyToMany
    @JoinTable(name = "apps_muted_by_users",
               joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
               inverseJoinColumns = @JoinColumn(name = "muted_app_id", referencedColumnName = "id"))
    private List<App> rawMutedApps = new LinkedList<>();

    @ManyToMany
    @JoinTable(name = "projects_archived_by_users",
               joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
               inverseJoinColumns = @JoinColumn(name = "archived_project_id", referencedColumnName = "id"))
    private List<Project> rawArchivedProjects = new LinkedList<>();

    private boolean isDeleted = false;

    // constructor without parameters is required by Hibernate
    @SuppressWarnings("unused")
    public User() {
    }

    public User(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public Integer id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String avatarHref() {
        return avatarHref;
    }

    public void setAvatarHref(String avatarHref) {
        this.avatarHref = avatarHref;
    }

    public boolean isVisibleForModerator() {
        return isVisibleForModerator;
    }

    public void setVisibleForModerator(boolean visibleForModerator) {
        isVisibleForModerator = visibleForModerator;
    }

    public boolean hasArchived(Project projectToCheck) {
        return archivedProjects().contains(projectToCheck);
    }

    private Set<Project> archivedProjects() {
        return rawArchivedProjects.stream()
                                  .filter(project -> !project.isDeleted())
                                  .collect(toSet());
    }

    public void archive(Project projectToArchive) {
        if (!rawArchivedProjects.contains(projectToArchive)) {
            rawArchivedProjects.add(projectToArchive);
        }
    }

    public void unarchive(Project projectToUnarchive) {
        rawArchivedProjects.remove(projectToUnarchive);
    }

    public boolean hasMuted(Project projectToCheck) {
        Set<App> apps = projectToCheck.apps();
        if (apps.isEmpty()) {
            return false;
        }
        return apps.stream()
                   .allMatch(app -> hasMuted(app));
    }

    public void mute(Project project) {
        project.apps().forEach(app -> mute(app));
    }

    public void unmute(Project project) {
        project.apps().forEach(app -> unmute(app));
    }

    public boolean hasMuted(App appToCheck) {
        return mutedApps().contains(appToCheck);
    }

    private Set<App> mutedApps() {
        return rawMutedApps.stream()
                           .filter(app -> !app.isDeleted())
                           .collect(toSet());
    }

    public void mute(App appToMute) {
        if (!rawMutedApps.contains(appToMute)) {
            rawMutedApps.add(appToMute);
        }
    }

    public void unmute(App appToUnmute) {
        rawMutedApps.remove(appToUnmute);
    }

    public boolean hasMarkedAsFavorite(Build buildToCheck) {
        return favoriteBuilds().contains(buildToCheck);
    }

    private Set<Build> favoriteBuilds() {
        return rawFavoriteBuilds.stream()
                                .filter(build -> !build.isDeleted())
                                .collect(toSet());
    }

    public void markBuildAsFavorite(Build build) {
        if (!rawFavoriteBuilds.contains(build)) {
            rawFavoriteBuilds.add(build);
        }
    }

    public void markBuildAsNotFavorite(Build build) {
        rawFavoriteBuilds.remove(build);
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    void delete() {
        isDeleted = true;
    }

}
