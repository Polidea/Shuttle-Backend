package com.polidea.shuttle.domain.user.output.factories;

import com.polidea.shuttle.domain.project.Project;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.output.AdminAssignedProjectResponse;
import com.polidea.shuttle.domain.user.output.AdminProjectAssigneeResponse;
import com.polidea.shuttle.domain.user.output.AdminUserResponse;
import com.polidea.shuttle.domain.user.permissions.global.GlobalPermission;
import com.polidea.shuttle.domain.user.permissions.global.GlobalPermissionsService;
import com.polidea.shuttle.domain.user.permissions.project.ProjectPermission;
import com.polidea.shuttle.domain.user.permissions.project.ProjectPermissionService;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class UserResponseFactory {

    private final GlobalPermissionsService globalPermissionsService;
    private final ProjectPermissionService projectPermissionService;

    public UserResponseFactory(GlobalPermissionsService globalPermissionsService,
                               ProjectPermissionService projectPermissionService) {
        this.globalPermissionsService = globalPermissionsService;
        this.projectPermissionService = projectPermissionService;
    }

    public AdminProjectAssigneeResponse createAdminProjectAssigneeResponse(User user, Project project) {
        Set<GlobalPermission> globalPermissions = globalPermissionsService.findFor(user);
        Collection<ProjectPermission> projectPermissions = projectPermissionService.findFor(user, project);
        return new AdminProjectAssigneeResponse(user, globalPermissions, projectPermissions);
    }

    public AdminUserResponse createAdminUserResponse(User user, Set<Project> projects) {
        List<AdminAssignedProjectResponse> assignedProjectResponses =
            projects.stream()
                    .map(assignedProject -> createAdminAssignedProjectResponse(user, assignedProject))
                    .collect(toList());
        return new AdminUserResponse(
            user,
            globalPermissionsService.findFor(user),
            assignedProjectResponses
        );
    }

    private AdminAssignedProjectResponse createAdminAssignedProjectResponse(User assignee, Project project) {
        Collection<ProjectPermission> projectPermissions = projectPermissionService.findFor(assignee, project);
        return new AdminAssignedProjectResponse(project, projectPermissions);
    }

}
