package com.polidea.shuttle.domain.user.output.factories;

import com.polidea.shuttle.domain.project.Project;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.output.AdminProjectAssigneeListResponse;
import com.polidea.shuttle.domain.user.output.AdminProjectAssigneeResponse;
import com.polidea.shuttle.domain.user.output.AdminUserListResponse;
import com.polidea.shuttle.domain.user.output.AdminUserResponse;
import com.polidea.shuttle.domain.user.permissions.global.GlobalPermissionsService;
import com.polidea.shuttle.domain.user.permissions.project.ProjectPermissionService;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class UserListResponseFactory {

    private final UserResponseFactory userResponseFactory;

    public UserListResponseFactory(GlobalPermissionsService globalPermissionsService,
                                   ProjectPermissionService projectPermissionService) {
        userResponseFactory = new UserResponseFactory(globalPermissionsService, projectPermissionService);
    }

    public AdminUserListResponse createAdminUserListResponse(Map<User, Set<Project>> usersAndTheirProjects) {
        List<AdminUserResponse> userResponses =
            usersAndTheirProjects.keySet()
                                 .stream()
                                 .map(user -> userResponseFactory.createAdminUserResponse(user, usersAndTheirProjects.get(user)))
                                 .collect(toList());
        return new AdminUserListResponse(userResponses);
    }

    public AdminProjectAssigneeListResponse createAdminProjectAssigneeListResponse(Project project) {
        List<AdminProjectAssigneeResponse> projectAssigneeResponses =
            project.assignees()
                   .stream()
                   .map(user -> userResponseFactory.createAdminProjectAssigneeResponse(user, project))
                   .collect(toList());
        return new AdminProjectAssigneeListResponse(projectAssigneeResponses);

    }

}
