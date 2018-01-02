package com.polidea.shuttle.data.datasets;

import com.polidea.shuttle.data.DataLoadHelper;
import com.polidea.shuttle.domain.user.access_token.TokenType;
import org.slf4j.Logger;

import static com.google.common.collect.Lists.newArrayList;
import static com.polidea.shuttle.domain.user.permissions.PermissionType.BUILD_CREATOR;
import static org.slf4j.LoggerFactory.getLogger;

public class ContinuousDeploymentUserData {

    private static final Logger LOGGER = getLogger(ContinuousDeploymentUserData.class);

    private final DataLoadHelper dataLoadHelper;
    private final String continuousDeploymentToken;

    public ContinuousDeploymentUserData(DataLoadHelper dataLoadHelper, String continuousDeploymentToken) {
        this.dataLoadHelper = dataLoadHelper;
        this.continuousDeploymentToken = continuousDeploymentToken;
    }

    public void load() {
        LOGGER.info("Loading data of Continuous Deployment User...");

        String continuousDeploymentUserEmail = "continuous.deployment@your-shuttle.com";
        dataLoadHelper.createUserIfMissing(
            continuousDeploymentUserEmail,
            "Continuous Deployment",
            "https://gitlab.com/uploads/group/avatar/6543/gitlab-logo-square.png"
        );
        dataLoadHelper.createOrRenewAccessToken(
            continuousDeploymentUserEmail,
            TokenType.CONTINUOUS_DEPLOYMENT,
            continuousDeploymentToken,
            emptyDeviceId()
        );
        dataLoadHelper.setGlobalPermissions(
            continuousDeploymentUserEmail,
            newArrayList(BUILD_CREATOR)
        );
    }

    private String emptyDeviceId() {
        return null;
    }

}
