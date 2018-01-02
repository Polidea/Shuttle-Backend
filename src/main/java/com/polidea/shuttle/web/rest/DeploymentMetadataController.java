package com.polidea.shuttle.web.rest;


import com.polidea.shuttle.domain.app.DeploymentService;
import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.app.input.DeploymentMetadataRequest;
import com.polidea.shuttle.domain.notifications.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@SuppressWarnings("unused")
@RestController
public class DeploymentMetadataController {

    private final DeploymentService deploymentService;
    private final NotificationService notificationService;

    @Autowired
    public DeploymentMetadataController(DeploymentService deploymentService, NotificationService notificationService) {
        this.deploymentService = deploymentService;
        this.notificationService = notificationService;
    }

    @RequestMapping(value = "/cd/apps/{platform}/{appId}/builds", method = POST)
    @PreAuthorize("@permissionChecks.check(principal).canCreateBuild().execute()")
    @ResponseStatus(OK)
    public void createBuild(@RequestBody @Valid DeploymentMetadataRequest deploymentMetadataRequest,
                            @PathVariable Platform platform,
                            @PathVariable String appId) {
        deploymentService.registerNewBuild(deploymentMetadataRequest, appId, platform);
        String buildIdentifier = deploymentMetadataRequest.build.getBuildIdentifier();
        notificationService.notifyAboutReleasedBuild(platform, appId, buildIdentifier);
    }
}
