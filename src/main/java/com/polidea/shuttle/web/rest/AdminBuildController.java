package com.polidea.shuttle.web.rest;

import com.polidea.shuttle.application.BuildPublisher;
import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.build.BuildService;
import com.polidea.shuttle.domain.build.NoPermissionToFetchBuildsException;
import com.polidea.shuttle.domain.build.output.AdminBuildListResponse;
import com.polidea.shuttle.domain.notifications.NotificationService;
import com.polidea.shuttle.infrastructure.security.authentication.AuthenticatedUser;
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/admin/projects/{projectId}/apps/{platform}/{appId:.+}/builds")
public class AdminBuildController {

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
    public AdminBuildController(BuildService buildService,
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
    @ResponseBody
    public AdminBuildListResponse getAppBuilds(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                               @PathVariable Integer projectId,
                                               @PathVariable Platform platform,
                                               @PathVariable String appId) {
        appPathValidation.assertValidPath(projectId, platform, appId);

        if (permissionChecks.check(authenticatedUser)
                            .canAdminister()
                            .or()
                            .canModerate(projectId)
                            .or()
                            .canPublish(projectId)
                            .or()
                            .canViewNotPublished(projectId)
                            .execute()) {
            return buildService.fetchAllAppBuilds(appId, platform);
        }

        if (permissionChecks.check(authenticatedUser).isUserAssignedToProject(projectId).execute()) {
            return buildService.fetchPublishedAppBuildsForAdmin(appId, platform);
        }

        throw new NoPermissionToFetchBuildsException(platform, appId);
    }

    @RequestMapping(value = "/{buildIdentifier:.+}", method = DELETE)
    @PreAuthorize("@permissionChecks.check(principal).canAdminister().or().canModerate(#appId, #platform).execute()")
    @ResponseStatus(NO_CONTENT)
    public void deleteBuild(@PathVariable Integer projectId,
                            @PathVariable Platform platform,
                            @PathVariable String appId,
                            @PathVariable String buildIdentifier) {
        buildPathValidation.assertValidPath(projectId, platform, appId, buildIdentifier);
        buildService.deleteBuild(platform, appId, buildIdentifier);
    }

    @RequestMapping(value = "/{buildIdentifier}/publish", method = POST)
    @PreAuthorize("@permissionChecks.check(principal).canPublish(#appId, #platform).execute()")
    @ResponseStatus(NO_CONTENT)
    public void publishBuild(@PathVariable Integer projectId,
                             @PathVariable Platform platform,
                             @PathVariable String appId,
                             @PathVariable String buildIdentifier) {
        buildPathValidation.assertValidPath(projectId, platform, appId, buildIdentifier);
        buildPublisher.publish(platform, appId, buildIdentifier);
    }

    @RequestMapping(value = "/{buildIdentifier}/publish", method = DELETE)
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
