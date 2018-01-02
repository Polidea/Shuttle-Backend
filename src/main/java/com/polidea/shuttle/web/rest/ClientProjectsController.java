package com.polidea.shuttle.web.rest;


import com.polidea.shuttle.domain.project.ProjectService;
import com.polidea.shuttle.domain.project.output.ClientArchivedProjectListResponse;
import com.polidea.shuttle.domain.project.output.ClientProjectListWithLatestBuildsResponse;
import com.polidea.shuttle.infrastructure.security.authentication.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/projects")
public class ClientProjectsController {

    private final ProjectService projectService;

    @Autowired
    public ClientProjectsController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @RequestMapping(method = GET)
    @ResponseStatus(OK)
    public ClientProjectListWithLatestBuildsResponse getAllProjects(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return projectService.fetchAllUserProjectsWithLatestBuilds(authenticatedUser.userEmail);
    }

    @RequestMapping(value = "/archived", method = GET)
    @ResponseStatus(OK)
    public ClientArchivedProjectListResponse getAllArchivedProjects(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return projectService.fetchAllArchivedUserProjects(authenticatedUser.userEmail);
    }

    @RequestMapping(value = "/{projectId}/mute", method = POST)
    @PreAuthorize("@permissionChecks.check(principal).canMute(#projectId).execute()")
    @ResponseStatus(NO_CONTENT)
    public void muteProject(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                            @PathVariable Integer projectId) {
        projectService.mute(projectId, authenticatedUser.userEmail);
    }

    @RequestMapping(value = "/{projectId}/mute", method = DELETE)
    @PreAuthorize("@permissionChecks.check(principal).canMute(#projectId).execute()")
    @ResponseStatus(NO_CONTENT)
    public void unmuteProject(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                              @PathVariable Integer projectId) {
        projectService.unmute(projectId, authenticatedUser.userEmail);
    }

    @RequestMapping(value = "/{projectId}/archive", method = POST)
    @PreAuthorize("@permissionChecks.check(principal).canArchive(#projectId).execute()")
    @ResponseStatus(NO_CONTENT)
    public void archiveProject(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                               @PathVariable Integer projectId) {
        projectService.archive(projectId, authenticatedUser.userEmail);
    }

    @RequestMapping(value = "/{projectId}/unarchive", method = POST)
    @ResponseStatus(NO_CONTENT)
    public void unarchiveProject(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                 @PathVariable Integer projectId) {
        projectService.unarchive(projectId, authenticatedUser.userEmail);
    }

}
