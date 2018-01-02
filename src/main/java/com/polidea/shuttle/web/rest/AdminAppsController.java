package com.polidea.shuttle.web.rest;

import com.polidea.shuttle.domain.app.AppService;
import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.app.input.AppAdditionRequest;
import com.polidea.shuttle.domain.app.input.AppEditionRequest;
import com.polidea.shuttle.domain.app.output.AdminAppListResponse;
import com.polidea.shuttle.error_codes.ForbiddenException;
import com.polidea.shuttle.infrastructure.security.authentication.AuthenticatedUser;
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/admin/projects/{projectId}/apps")
public class AdminAppsController {

    private final AppService appService;
    private final PermissionChecks permissionChecks;
    private final AppPathValidation appPathValidation;

    @Autowired
    public AdminAppsController(AppService appService,
                               PermissionChecks permissionChecks,
                               AppPathValidation appPathValidation) {
        this.appService = appService;
        this.permissionChecks = permissionChecks;
        this.appPathValidation = appPathValidation;
    }

    @RequestMapping(method = POST, value = "/{platform}/{appId:.+}")
    @PreAuthorize("@permissionChecks.check(principal).canAdminister().or().canModerate(#projectId).execute()")
    @ResponseStatus(NO_CONTENT)
    public void addApp(@RequestBody @Valid AppAdditionRequest appAdditionRequest,
                       @PathVariable Integer projectId,
                       @PathVariable Platform platform,
                       @PathVariable String appId) {
        appService.addApp(appAdditionRequest, projectId, platform, appId);
    }

    @RequestMapping(method = DELETE, value = "/{platform}/{appId:.+}")
    @PreAuthorize("@permissionChecks.check(principal).canAdminister().or().canModerate(#projectId).execute()")
    @ResponseStatus(NO_CONTENT)
    public void removeApp(@PathVariable Integer projectId,
                          @PathVariable Platform platform,
                          @PathVariable String appId) {
        appPathValidation.assertValidPath(projectId, platform, appId);
        appService.delete(appId, platform);
    }

    @RequestMapping(method = GET, value = "/{platform}")
    @ResponseStatus(OK)
    @ResponseBody
    public AdminAppListResponse getApps(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                        @PathVariable Integer projectId,
                                        @PathVariable Platform platform) {
        if (checkIfGlobalAdminOrAssignedProject(authenticatedUser, projectId)) {
            return appService.fetchAllApps(projectId, platform, authenticatedUser.userEmail);
        } else {
            throw new ForbiddenException(format("You are not assigned to project of id: %s", projectId));
        }
    }

    @RequestMapping(method = PATCH, value = "/{platform}/{appId:.+}")
    @PreAuthorize("@permissionChecks.check(principal).canAdminister().or().canModerate(#appId, #platform).execute()")
    @ResponseStatus(NO_CONTENT)
    public void editApp(@RequestBody @Valid AppEditionRequest nameEditionRequest,
                        @PathVariable Platform platform,
                        @PathVariable String appId,
                        @PathVariable Integer projectId) {
        appPathValidation.assertValidPath(projectId, platform, appId);
        appService.editApp(nameEditionRequest, platform, appId);
    }

    private Boolean checkIfGlobalAdminOrAssignedProject(AuthenticatedUser authenticatedUser, Integer projectId) {
        return permissionChecks.check(authenticatedUser).canAdminister().execute()
            || permissionChecks.check(authenticatedUser).isUserAssignedToProject(projectId).execute();
    }
}
