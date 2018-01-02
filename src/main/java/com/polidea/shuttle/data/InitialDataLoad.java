package com.polidea.shuttle.data;

import com.polidea.shuttle.data.datasets.AdditionalData;
import com.polidea.shuttle.data.datasets.ContinuousDeploymentUserData;
import com.polidea.shuttle.data.datasets.ShuttleBaseBuildsData;
import com.polidea.shuttle.data.datasets.ShuttleTeamDemoData;
import com.polidea.shuttle.data.datasets.UsersWithAccessToAdminPanelData;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import static org.slf4j.LoggerFactory.getLogger;

@Configuration
class InitialDataLoad implements ApplicationRunner {

    private static final Logger LOGGER = getLogger(InitialDataLoad.class);

    private final Environment environment;
    private final DataLoadHelper dataLoadHelper;

    @Value("${shuttle.tokens.continuous-deployment}")
    private String continuousDeploymentToken;
    @Value("${shuttle.app-id.ios}")
    private String shuttleIosAppId;
    @Value("${shuttle.app-id.android}")
    private String shuttleAndroidAppId;

    @Autowired
    public InitialDataLoad(Environment environment, DataLoadHelper dataLoadHelper) {
        this.environment = environment;
        this.dataLoadHelper = dataLoadHelper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (environment.acceptsProfiles("mandatoryData")) {
            LOGGER.info("Loading mandatory data into database...");
            new UsersWithAccessToAdminPanelData(dataLoadHelper).load();
            new ShuttleBaseBuildsData(
                dataLoadHelper,
                shuttleIosAppId,
                shuttleAndroidAppId
            ).load();
            new ContinuousDeploymentUserData(dataLoadHelper, continuousDeploymentToken).load();
        }
        if (environment.acceptsProfiles("developmentData")) {
            LOGGER.info("Loading development data into database...");
            new ShuttleTeamDemoData(dataLoadHelper).load();
            new AdditionalData(dataLoadHelper).load();
        }
    }

}
