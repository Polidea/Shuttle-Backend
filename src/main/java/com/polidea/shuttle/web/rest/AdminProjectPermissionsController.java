package com.polidea.shuttle.web.rest;

import com.polidea.shuttle.domain.user.permissions.PermissionType;
import com.polidea.shuttle.domain.user.permissions.project.ProjectPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/admin/projects")
public class AdminProjectPermissionsController {

    private final ProjectPermissionService projectPermissionService;

    @Autowired
    public AdminProjectPermissionsController(ProjectPermissionService projectPermissionService) {
        this.projectPermissionService = projectPermissionService;
    }

    @RequestMapping(method = POST, value = "/{projectId:.+}/users/{assigneeEmail:.+}/permission/{permissionType}")
    @PreAuthorize("@permissionChecks.check(principal).canSetProjectPermission(#projectId, #permissionType).execute()")
    @ResponseStatus(NO_CONTENT)
    public void assignPerProjectPermission(@PathVariable String assigneeEmail,
                                           @PathVariable Integer projectId,
                                           @PathVariable PermissionType permissionType) {
        projectPermissionService.assignProjectPermission(permissionType, assigneeEmail, projectId);
    }

    @RequestMapping(method = DELETE, value = "/{projectId:.+}/users/{assigneeEmail:.+}/permission/{permissionType}")
    @PreAuthorize("@permissionChecks.check(principal).canSetProjectPermission(#projectId, #permissionType).execute()")
    @ResponseStatus(NO_CONTENT)
    public void unassignPerProjectPermission(@PathVariable String assigneeEmail,
                                             @PathVariable Integer projectId,
                                             @PathVariable PermissionType permissionType) {
        projectPermissionService.unassignProjectPermission(permissionType, assigneeEmail, projectId);
    }

}
