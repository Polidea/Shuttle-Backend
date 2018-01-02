package com.polidea.shuttle.infrastructure.security.authorization;

import com.polidea.shuttle.domain.app.App;
import com.polidea.shuttle.domain.app.AppNotFoundException;
import com.polidea.shuttle.domain.app.AppRepository;
import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.project.Project;
import com.polidea.shuttle.domain.project.ProjectNotFoundException;
import com.polidea.shuttle.domain.project.ProjectRepository;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.UserNotFoundException;
import com.polidea.shuttle.domain.user.UserRepository;
import com.polidea.shuttle.domain.user.permissions.PermissionType;
import com.polidea.shuttle.domain.user.permissions.global.GlobalPermissionRepository;
import com.polidea.shuttle.domain.user.permissions.project.ProjectPermissionRepository;
import com.polidea.shuttle.infrastructure.security.authentication.AuthenticatedUser;

import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;

@SuppressWarnings("unused")
public class PermissionChecks {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final AppRepository appRepository;
    private final GlobalPermissionRepository globalPermissionRepository;
    private final ProjectPermissionRepository projectPermissionRepository;

    private List<Object> values;
    private User user;

    public PermissionChecks(UserRepository userRepository,
                            ProjectRepository projectRepository,
                            AppRepository appRepository,
                            GlobalPermissionRepository globalPermissionRepository,
                            ProjectPermissionRepository projectPermissionRepository) {
        this.userRepository = userRepository;
        this.appRepository = appRepository;
        this.projectRepository = projectRepository;
        this.globalPermissionRepository = globalPermissionRepository;
        this.projectPermissionRepository = projectPermissionRepository;
    }

    // TODO extract this logic and test it
    public Boolean execute() {
        Boolean isAuthorized = null;
        int index = 0;

        while (index < values.size()) {
            Object value = values.get(index);
            if (value instanceof Boolean && isAuthorized == null) {
                isAuthorized = (Boolean) value;
                index++;
            } else if (value instanceof PermissionOperator) {
                if (value == PermissionOperator.OR) {
                    isAuthorized = isAuthorized || (Boolean) values.get(index + 1);
                }
                index += 2;
            } else {
                index++;
            }
        }

        if (isAuthorized == null) {
            isAuthorized = false;
        }

        return isAuthorized;
    }

    public PermissionChecks check(AuthenticatedUser authenticatedUser) {
        return check(findUser(authenticatedUser.userEmail));
    }

    public PermissionChecks check(User user) {
        this.user = user;
        this.values = newLinkedList();
        return this;
    }

    private User findUser(String userEmail) {
        return userRepository.findUser(userEmail)
                             .orElseThrow(() -> new UserNotFoundException(userEmail));
    }

    public PermissionChecks or() {
        values.add(PermissionOperator.OR);
        return this;
    }

    private Boolean checkPermissionProject(Project project, PermissionType permissionType) {
        if (project == null) {
            throw new ProjectNotFoundException();
        }
        return projectPermissionRepository.findByUserAndProject(user, project)
                                          .stream()
                                          .anyMatch(projectPermission -> projectPermission.isOfType(permissionType));
    }

    private Boolean checkPermission(Project project, PermissionType permissionType) {
        Boolean permissionGlobal = checkPermission(permissionType);
        Boolean permissionProject = checkPermissionProject(project, permissionType);
        return permissionGlobal || permissionProject;
    }

    private Boolean checkPermission(PermissionType permissionType) {
        return globalPermissionRepository.findFor(user)
                                         .stream()
                                         .anyMatch(globalPermission -> globalPermission.isOfType(permissionType));
    }

    public PermissionChecks canModerate(Integer projectId) {
        values.add(canModerateValue(projectId));
        return this;
    }

    public PermissionChecks canModerate(String appId, Platform platform) {
        values.add(canModerateValue(appId, platform));
        return this;
    }

    public PermissionChecks canModerateAtLeastOneProject() {
        values.add(canModerateAtLeastOneProjectValue());
        return this;
    }

    public PermissionChecks canPublish(String appId, Platform platform) {
        values.add(canPublishValue(appId, platform));
        return this;
    }

    public PermissionChecks canPublish(Integer projectId) {
        values.add(canPublishValue(projectId));
        return this;
    }

    public PermissionChecks canViewNotPublished(String appId, Platform platform) {
        values.add(canViewNotPublishedValue(appId, platform));
        return this;
    }

    public PermissionChecks canViewNotPublished(Integer projectId) {
        values.add(canViewNotPublishedValue(projectId));
        return this;
    }

    public PermissionChecks canAdminister() {
        values.add(checkPermission(PermissionType.ADMIN));
        return this;
    }

    public PermissionChecks hasEmail(String email) {
        values.add(user.email().equalsIgnoreCase(email));
        return this;
    }

    public PermissionChecks canCreateBuild() {
        values.add(checkPermission(PermissionType.BUILD_CREATOR));
        return this;
    }

    public PermissionChecks canArchive(Integer projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException());
        values.add(checkPermission(project, PermissionType.ARCHIVER));
        return this;
    }

    public PermissionChecks canMute(Integer projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException());
        values.add(checkPermission(project, PermissionType.MUTER));
        return this;
    }

    public PermissionChecks canMute(Platform platform, String appId) {
        App app = findApp(platform, appId);
        values.add(checkPermission(app.project(), PermissionType.MUTER));
        return this;
    }

    private Boolean canModerateAtLeastOneProjectValue() {
        return projectPermissionRepository.findByUser(user).size() > 0;
    }

    private Boolean canModerateValue(Integer projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException());
        return checkPermissionProject(project, PermissionType.ADMIN);
    }

    private Boolean canModerateValue(String appId, Platform platform) {
        App app = findApp(platform, appId);
        return checkPermissionProject(app.project(), PermissionType.ADMIN);
    }

    private Boolean canPublishValue(String appId, Platform platform) {
        App app = findApp(platform, appId);
        return checkPermission(app.project(), PermissionType.PUBLISHER);
    }

    private Boolean canPublishValue(Integer projectId) {
        Project project = projectRepository.findById(projectId)
                                           .orElseThrow(() -> new ProjectNotFoundException());
        return checkPermission(project, PermissionType.PUBLISHER);
    }

    private Boolean canViewNotPublishedValue(String appId, Platform platform) {
        App app = findApp(platform, appId);
        return checkPermission(app.project(), PermissionType.UNPUBLISHED_BUILDS_VIEWER);
    }

    private Boolean canViewNotPublishedValue(Integer projectId) {
        Project project = projectRepository.findById(projectId)
                                           .orElseThrow(() -> new ProjectNotFoundException());
        return checkPermission(project, PermissionType.UNPUBLISHED_BUILDS_VIEWER);
    }

    private Boolean canAdministerValue() {
        return checkPermission(PermissionType.ADMIN);
    }


    private Boolean canSetProjectPermissionValue(Integer projectId, PermissionType permissionTypeToSet) {
        if (permissionTypeToSet == PermissionType.ADMIN) {
            return canAdministerValue() || canModerateValue(projectId);
        }
        if (permissionTypeToSet == PermissionType.PUBLISHER
            || permissionTypeToSet == PermissionType.MUTER
            || permissionTypeToSet == PermissionType.ARCHIVER
            || permissionTypeToSet == PermissionType.UNPUBLISHED_BUILDS_VIEWER) {
            return canAdministerValue() || canModerateValue(projectId);
        }
        return false;
    }

    public PermissionChecks canSetProjectPermission(Integer projectId, PermissionType permissionType) {
        values.add(canSetProjectPermissionValue(projectId, permissionType));
        return this;
    }

    public PermissionChecks isUserAssignedToProject(Integer projectId) {
        values.add(isUserAssignedToProjectValue(projectId));
        return this;
    }

    private Boolean isUserAssignedToProjectValue(Integer projectId) {
        Project project = projectRepository.findById(projectId)
                                           .orElseThrow(() -> new ProjectNotFoundException());
        return project.hasAssigned(user);
    }

    private App findApp(Platform platform, String appId) {
        return appRepository.find(platform, appId)
                            .orElseThrow(() -> new AppNotFoundException(platform, appId));
    }

    private enum PermissionOperator {
        OR
    }
}
