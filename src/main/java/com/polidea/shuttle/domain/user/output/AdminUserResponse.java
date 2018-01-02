package com.polidea.shuttle.domain.user.output;

import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.permissions.PermissionType;
import com.polidea.shuttle.domain.user.permissions.global.GlobalPermission;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class AdminUserResponse {

    public final String email;

    public final String name;

    public final String avatarHref;

    public final boolean isVisibleForModerator;

    public final List<PermissionType> globalPermissions;

    public final List<AdminAssignedProjectResponse> projects;

    public AdminUserResponse(User user,
                             Set<GlobalPermission> globalPermissions,
                             List<AdminAssignedProjectResponse> userProjectResponses) {
        this.email = user.email();
        this.name = user.name();
        this.isVisibleForModerator = user.isVisibleForModerator();
        this.avatarHref = user.avatarHref();
        this.globalPermissions = globalPermissions.stream()
                                                  .map(GlobalPermission::type)
                                                  .collect(toList());
        this.projects = userProjectResponses;
    }

}
