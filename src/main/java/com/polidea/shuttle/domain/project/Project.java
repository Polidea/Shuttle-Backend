package com.polidea.shuttle.domain.project;

import com.polidea.shuttle.domain.app.App;
import com.polidea.shuttle.domain.build.Build;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.infrastructure.database.BaseEntity;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toSet;

@Entity(name = "projects")
public class Project extends BaseEntity {

    @NotBlank
    private String name;

    private String iconHref;

    @ManyToMany
    @JoinTable(name = "users_assigned_to_projects",
               joinColumns = @JoinColumn(name = "project_id", referencedColumnName = "id"),
               inverseJoinColumns = @JoinColumn(name = "assignee_id", referencedColumnName = "id"))
    private List<User> rawAssignees = new LinkedList<>();

    @ManyToMany
    @JoinTable(name = "project_members",
               joinColumns = @JoinColumn(name = "project_id", referencedColumnName = "id"),
               inverseJoinColumns = @JoinColumn(name = "member_id", referencedColumnName = "id"))
    private List<User> rawMembers = new LinkedList<>();

    @OneToMany(fetch = FetchType.LAZY,
               mappedBy = "project",
               cascade = CascadeType.ALL)
    private List<App> rawApps = new LinkedList<>();

    private boolean isDeleted = false;

    // constructor without parameters is required by Hibernate
    @SuppressWarnings("unused")
    public Project() {
    }

    public Project(String name) {
        this.name = name;
    }

    public Integer id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<App> apps() {
        return rawApps.stream()
                      .filter(app -> !app.isDeleted())
                      .collect(toSet());
    }

    public boolean shouldUserByNotifiedAboutAssignment(User user) {
        return !user.hasMuted(this);
    }

    public Set<User> usersToNotifyAboutPublishedBuild(Build build) {
        return assignees().stream()
                          .filter(assignee -> !assignee.hasArchived(build.app().project()))
                          .filter(assignee -> !assignee.hasMuted(build.app()))
                          .collect(toSet());
    }

    public Set<User> usersToNotifyAboutReleasedBuild(Build build) {
        return assignees().stream()
                          .filter(assignee -> !assignee.hasArchived(build.app().project()))
                          .filter(assignee -> !assignee.hasMuted(build.app()))
                          .collect(toSet());
    }

    public boolean hasAssigned(User user) {
        return assignees().contains(user);
    }

    public Set<User> assignees() {
        return rawAssignees.stream()
                           .filter(assignee -> !assignee.isDeleted())
                           .collect(toSet());
    }

    public void assign(User userToAssign) {
        if (!rawAssignees.contains(userToAssign)) {
            rawAssignees.add(userToAssign);
        }
    }

    public void unassign(User userToUnassign) {
        rawAssignees.remove(userToUnassign);
    }

    public Set<User> members() {
        return rawMembers.stream()
                         .filter(member -> !member.isDeleted())
                         .collect(toSet());
    }

    public void addMember(User memberToAdd) {
        if (!rawMembers.contains(memberToAdd)) {
            rawMembers.add(memberToAdd);
        }
    }

    public void removeMember(User memberToRemove) {
        rawMembers.remove(memberToRemove);
    }

    public String iconHref() {
        return iconHref;
    }

    public void setIconHref(String iconHref) {
        this.iconHref = iconHref;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public Optional<Long> lastReleaseDate(boolean canViewNotPublished) {
        Optional<Build> latestBuild =
            apps().stream()
                  .map(app -> app.lastBuild(canViewNotPublished))
                  .filter(Optional::isPresent)
                  .map(Optional::get)
                  .max(comparingLong(Build::releaseDate));

        return Optional.ofNullable(
            latestBuild.map(build -> build.releaseDate())
                       .orElse(null)
        );
    }

    void delete() {
        apps().forEach(app -> app.delete());
        isDeleted = true;
    }
}

