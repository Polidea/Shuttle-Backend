package com.polidea.shuttle.web.rest;


import com.polidea.shuttle.domain.app.AppService;
import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.app.output.ClientAppListResponse;
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
@RequestMapping("/projects/{projectId}/apps/{platform}")
public class ClientAppsController {

    private final AppService appService;
    private final AppPathValidation appPathValidation;

    @Autowired
    public ClientAppsController(AppService appService, AppPathValidation appPathValidation) {
        this.appService = appService;
        this.appPathValidation = appPathValidation;
    }

    @RequestMapping(method = GET)
    @PreAuthorize("@permissionChecks.check(principal).isUserAssignedToProject(#projectId).execute()")
    @ResponseStatus(OK)
    public ClientAppListResponse getAppsDetails(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                @PathVariable Platform platform,
                                                @PathVariable Integer projectId) {
        return appService.fetchAllAppsDetails(projectId, platform, authenticatedUser.userEmail);
    }

    @RequestMapping(value = "/{appId}/mute", method = POST)
    @PreAuthorize("@permissionChecks.check(principal).canMute(#platform, #appId).execute()")
    @ResponseStatus(NO_CONTENT)
    public void muteApp(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                        @PathVariable Platform platform,
                        @PathVariable String appId,
                        @PathVariable Integer projectId) {
        appPathValidation.assertValidPath(projectId, platform, appId);
        appService.mute(appId, platform, authenticatedUser.userEmail);
    }

    @RequestMapping(value = "/{appId}/mute", method = DELETE)
    @PreAuthorize("@permissionChecks.check(principal).canMute(#platform, #appId).execute()")
    @ResponseStatus(NO_CONTENT)
    public void unmuteApp(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                          @PathVariable Platform platform,
                          @PathVariable String appId,
                          @PathVariable Integer projectId) {
        appPathValidation.assertValidPath(projectId, platform, appId);
        appService.unmute(appId, platform, authenticatedUser.userEmail);
    }

}
