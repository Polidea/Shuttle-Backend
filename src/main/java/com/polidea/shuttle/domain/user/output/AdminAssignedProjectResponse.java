package com.polidea.shuttle.domain.user.output;

import com.polidea.shuttle.domain.project.Project;
import com.polidea.shuttle.domain.user.permissions.PermissionType;
import com.polidea.shuttle.domain.user.permissions.project.ProjectPermission;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AdminAssignedProjectResponse {

    public final Integer id;

    public final String name;

    public final String iconHref;

    public final List<PermissionType> permissions;

    public AdminAssignedProjectResponse(Project assignedProject, Collection<ProjectPermission> projectPermissions) {
        this.id = assignedProject.id();
        this.name = assignedProject.name();
        this.iconHref = assignedProject.iconHref();
        this.permissions = projectPermissions.stream()
                                             .map(ProjectPermission::type)
                                             .collect(Collectors.toList());
    }

}
