package com.polidea.shuttle.web.rest;

import com.polidea.shuttle.domain.build.BuildService;
import com.polidea.shuttle.domain.project.Project;
import com.polidea.shuttle.domain.project.ProjectService;
import com.polidea.shuttle.domain.shuttle.output.AdminShuttleAllBuildsResponse;
import com.polidea.shuttle.domain.shuttle.output.AdminShuttleBuildsResponse;
import com.polidea.shuttle.domain.shuttle.output.AdminShuttlePublishedBuildsResponse;
import com.polidea.shuttle.infrastructure.security.authentication.AuthenticatedUser;
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@SuppressWarnings("unused")
@RestController
public class AdminShuttleBuildsController {

    private final BuildService buildService;
    private final ProjectService projectService;
    private final PermissionChecks permissionChecks;

    @Value("${shuttle.project.name}")
    private String shuttleProjectName;

    public AdminShuttleBuildsController(BuildService buildService,
                                        ProjectService projectService,
                                        PermissionChecks permissionChecks) {
        this.buildService = buildService;
        this.projectService = projectService;
        this.permissionChecks = permissionChecks;
    }

    @RequestMapping(value = "/admin/shuttle/builds/published", method = GET)
    @ResponseStatus(OK)
    public AdminShuttlePublishedBuildsResponse getShuttlePublishedBuilds() {
        AdminShuttleBuildsResponse newestPublishedBuilds = buildService.findLatestPublishedShuttleBuildsForAdmin();
        return new AdminShuttlePublishedBuildsResponse(newestPublishedBuilds);
    }

    @RequestMapping(value = "/admin/shuttle/builds", method = GET)
    @ResponseStatus(OK)
    public AdminShuttleAllBuildsResponse getShuttleBuilds(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        AdminShuttleBuildsResponse newestPublishedBuilds = buildService.findLatestPublishedShuttleBuildsForAdmin();
        AdminShuttleBuildsResponse newestBuilds = null;

        if (checkIfPublisherOfShuttle(authenticatedUser)) {
            newestBuilds = buildService.findLatestShuttleBuildsForAdmin();
        }

        return new AdminShuttleAllBuildsResponse(
            newestPublishedBuilds,
            newestBuilds
        );
    }

    private Boolean checkIfPublisherOfShuttle(AuthenticatedUser authenticatedUser) {
        Project shuttleProject = projectService.findByName(shuttleProjectName);
        return permissionChecks.check(authenticatedUser)
                               .canPublish(shuttleProject.id())
                               .or()
                               .canViewNotPublished(shuttleProject.id())
                               .execute();
    }

}
