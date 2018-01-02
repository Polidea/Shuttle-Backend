package com.polidea.shuttle.domain.app;

import com.polidea.shuttle.domain.app.input.DeploymentMetadataRequest;
import com.polidea.shuttle.domain.build.BuildService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeploymentService {

    private final AppService appService;
    private final BuildService buildService;

    @Autowired
    public DeploymentService(AppService appService, BuildService buildService) {
        this.appService = appService;
        this.buildService = buildService;
    }

    public void registerNewBuild(DeploymentMetadataRequest deploymentMetadataRequest, String appId, Platform platform) {
        App app = appService.findApp(platform, appId);
        buildService.addBuild(deploymentMetadataRequest.build, app);
    }
}
