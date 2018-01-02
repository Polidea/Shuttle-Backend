package com.polidea.shuttle.web.rest;

import com.polidea.shuttle.domain.app.input.ProjectAdditionRequest;
import com.polidea.shuttle.domain.app.input.ProjectEditionRequest;
import com.polidea.shuttle.domain.project.Project;
import com.polidea.shuttle.domain.project.ProjectService;
import com.polidea.shuttle.domain.project.output.AdminProjectIdResponse;
import com.polidea.shuttle.domain.project.output.AdminProjectListResponse;
import com.polidea.shuttle.infrastructure.security.authentication.AuthenticatedUser;
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/admin/projects")
public class AdminProjectsController {

    private final ProjectService projectService;
    private final PermissionChecks permissionChecks;

    @Autowired
    public AdminProjectsController(ProjectService projectService,
                                   PermissionChecks permissionChecks) {
        this.projectService = projectService;
        this.permissionChecks = permissionChecks;
    }

    @RequestMapping(method = POST)
    @PreAuthorize("@permissionChecks.check(principal).canAdminister().execute()")
    @ResponseStatus(OK)
    public AdminProjectIdResponse addProject(@RequestBody @Valid ProjectAdditionRequest projectAdditionRequest) {
        Project project = projectService.addProject(projectAdditionRequest);
        return new AdminProjectIdResponse(project.id());
    }

    @RequestMapping(method = GET)
    @ResponseStatus(OK)
    public AdminProjectListResponse getAllProjects(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        if (permissionChecks.check(authenticatedUser).canAdminister().execute()) {
            return projectService.fetchAllProjects(authenticatedUser.userEmail);
        }
        return projectService.fetchAllUserProjectsForAdmin(authenticatedUser.userEmail);
    }

    @RequestMapping(method = PATCH, value = "/{projectId}")
    @PreAuthorize("@permissionChecks.check(principal).canAdminister().or().canModerate(#projectId).execute()")
    @ResponseStatus(NO_CONTENT)
    public void editProject(@RequestBody @Valid ProjectEditionRequest projectEditionRequest,
                            @PathVariable Integer projectId) {
        projectService.editProject(projectId, projectEditionRequest);
    }

    @RequestMapping(method = DELETE, value = "/{projectId}")
    @PreAuthorize("@permissionChecks.check(principal).canAdminister().execute()")
    @ResponseStatus(NO_CONTENT)
    public void deleteProject(@PathVariable Integer projectId) {
        projectService.deleteProject(projectId);
    }

    @RequestMapping(method = POST, value = "/{projectId}/members/{email:.+}")
    @ResponseStatus(OK)
    public void addMemberToProject(@PathVariable Integer projectId,
                                   @PathVariable String email) {
        projectService.addMember(projectId, email);
    }

    @RequestMapping(method = DELETE, value = "/{projectId}/members/{email:.+}")
    @ResponseStatus(OK)
    public void deleteMemberFromProject(@PathVariable Integer projectId,
                                        @PathVariable String email) {
        projectService.removeMember(projectId, email);
    }
}
