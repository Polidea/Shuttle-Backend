package com.polidea.shuttle.web.rest;

import com.polidea.shuttle.application.BuildPublisher;
import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.build.BuildService;
import com.polidea.shuttle.domain.build.NoPermissionToFetchBuildsException;
import com.polidea.shuttle.domain.build.output.ClientBuildListResponse;
import com.polidea.shuttle.domain.notifications.NotificationService;
import com.polidea.shuttle.infrastructure.security.authentication.AuthenticatedUser;
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@RequestMapping("/projects/{projectId}/apps/{platform}/{appId:.+}/builds")
public class ClientBuildController {

    @Value("${shuttle.app-id.ios}")
    private String shuttleIosAppId;
    @Value("${shuttle.app-id.android}")
    private String shuttleAndroidAppId;

    private final BuildService buildService;
    private final PermissionChecks permissionChecks;
    private final BuildPathValidation buildPathValidation;
    private final AppPathValidation appPathValidation;
    private final BuildPublisher buildPublisher;

    @Autowired
    public ClientBuildController(BuildService buildService,
                                 PermissionChecks permissionChecks,
                                 BuildPathValidation buildPathValidation,
                                 AppPathValidation appPathValidation,
                                 NotificationService notificationService) {
        this.buildService = buildService;
        this.permissionChecks = permissionChecks;
        this.buildPathValidation = buildPathValidation;
        this.appPathValidation = appPathValidation;
        this.buildPublisher = new BuildPublisher(
            shuttleIosAppId,
            shuttleAndroidAppId,
            buildService,
            notificationService
        );
    }

    @RequestMapping(method = GET)
    @ResponseStatus(OK)
    public ClientBuildListResponse getAppBuilds(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                @PathVariable Integer projectId,
                                                @PathVariable Platform platform,
                                                @PathVariable String appId) {
        appPathValidation.assertValidPath(projectId, platform, appId);
        if (permissionChecks.check(authenticatedUser)
                            .canPublish(projectId)
                            .or()
                            .canViewNotPublished(projectId)
                            .execute()) {
            return buildService.fetchAppBuildsForPublisher(appId, platform, authenticatedUser.userEmail);
        }
        if (permissionChecks.check(authenticatedUser).isUserAssignedToProject(projectId).execute()) {
            return buildService.fetchPublishedAppBuilds(appId, platform, authenticatedUser.userEmail);
        }
        throw new NoPermissionToFetchBuildsException(platform, appId);
    }

    @RequestMapping(method = POST, value = "/{buildIdentifier}/favorite")
    @PreAuthorize("@permissionChecks.check(principal).isUserAssignedToProject(#projectId).execute()")
    @ResponseStatus(NO_CONTENT)
    public void favoriteBuild(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                              @PathVariable Integer projectId,
                              @PathVariable Platform platform,
                              @PathVariable String appId,
                              @PathVariable String buildIdentifier) {
        buildPathValidation.assertValidPath(projectId, platform, appId, buildIdentifier);
        buildService.favoriteBuild(authenticatedUser.userEmail, platform, appId, buildIdentifier);
    }

    @RequestMapping(method = DELETE, value = "/{buildIdentifier}/favorite")
    @PreAuthorize("@permissionChecks.check(principal).isUserAssignedToProject(#projectId).execute()")
    @ResponseStatus(NO_CONTENT)
    public void unfavoriteBuild(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                @PathVariable Integer projectId,
                                @PathVariable Platform platform,
                                @PathVariable String appId,
                                @PathVariable String buildIdentifier) {
        buildPathValidation.assertValidPath(projectId, platform, appId, buildIdentifier);
        buildService.unfavoriteBuild(authenticatedUser.userEmail, platform, appId, buildIdentifier);
    }

    @RequestMapping(method = POST, value = "/{buildIdentifier}/publish")
    @PreAuthorize("@permissionChecks.check(principal).canPublish(#appId, #platform).execute()")
    @ResponseStatus(NO_CONTENT)
    public void publishBuild(@PathVariable Integer projectId,
                             @PathVariable Platform platform,
                             @PathVariable String appId,
                             @PathVariable String buildIdentifier) {
        buildPathValidation.assertValidPath(projectId, platform, appId, buildIdentifier);
        buildPublisher.publish(platform, appId, buildIdentifier);
    }

    @RequestMapping(method = DELETE, value = "/{buildIdentifier}/publish")
    @PreAuthorize("@permissionChecks.check(principal).canPublish(#appId, #platform).execute()")
    @ResponseStatus(NO_CONTENT)
    public void unpublishBuild(@PathVariable Integer projectId,
                               @PathVariable Platform platform,
                               @PathVariable String appId,
                               @PathVariable String buildIdentifier) {
        buildPathValidation.assertValidPath(projectId, platform, appId, buildIdentifier);
        buildService.unpublishBuild(platform, appId, buildIdentifier);
    }

}

