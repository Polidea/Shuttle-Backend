package com.polidea.shuttle.domain.user.output;

import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.permissions.PermissionType;
import com.polidea.shuttle.domain.user.permissions.global.GlobalPermission;
import com.polidea.shuttle.domain.user.permissions.project.ProjectPermission;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class AdminProjectAssigneeResponse {

    public final String email;

    public final String name;

    public final String avatarHref;

    public final List<PermissionType> globalPermissions;

    public final List<PermissionType> projectPermissions;

    public AdminProjectAssigneeResponse(User user,
                                        Set<GlobalPermission> globalPermissions,
                                        Collection<ProjectPermission> projectPermissions) {
        this.email = user.email();
        this.name = user.name();
        this.avatarHref = user.avatarHref();
        this.globalPermissions = globalPermissions.stream()
                                                  .map(GlobalPermission::type)
                                                  .collect(toList());
        this.projectPermissions = projectPermissions.stream()
                                                    .map(ProjectPermission::type)
                                                    .collect(toList());
    }
}
