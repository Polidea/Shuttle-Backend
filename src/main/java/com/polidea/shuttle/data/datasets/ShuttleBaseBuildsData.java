package com.polidea.shuttle.data.datasets;

import com.polidea.shuttle.data.DataLoadHelper;
import org.slf4j.Logger;

import static com.polidea.shuttle.domain.app.Platform.ANDROID;
import static com.polidea.shuttle.domain.app.Platform.IOS;
import static org.slf4j.LoggerFactory.getLogger;

public class ShuttleBaseBuildsData {

    private static final Logger LOGGER = getLogger(ShuttleBaseBuildsData.class);

    private final DataLoadHelper dataLoadHelper;
    private final String shuttleIosAppId;
    private final String shuttleAndroidAppId;

    public ShuttleBaseBuildsData(DataLoadHelper dataLoadHelper, String shuttleIosAppId, String shuttleAndroidAppId) {
        this.dataLoadHelper = dataLoadHelper;
        this.shuttleIosAppId = shuttleIosAppId;
        this.shuttleAndroidAppId = shuttleAndroidAppId;
    }

    public void load() {
        LOGGER.info("Loading data of Shuttle base builds...");

        int projectId = dataLoadHelper.createProjectIfMissing("Shuttle");

        dataLoadHelper.createAppIfMissing(projectId, ANDROID, shuttleAndroidAppId, "Shuttle");
        dataLoadHelper.createAppIfMissing(projectId, IOS, shuttleIosAppId, "Shuttle");

        dataLoadHelper.createBuildIfMissing(
            ANDROID, shuttleAndroidAppId, "1", "25-08 16:37-698186e", 123456L,
            "https://url-to-app/Shuttle/android/app.apk",
            "admin@your-shuttle.com");
        dataLoadHelper.publishBuild(ANDROID, shuttleAndroidAppId, "1");

        dataLoadHelper.createBuildIfMissing(
            IOS, shuttleIosAppId, "ABCDefgh", "1.0", 234567L,
            "itms-services://?action=download-manifest&url=https://url-to-app/Shuttle.plist",
            "admin@your-shuttle.com");
        dataLoadHelper.publishBuild(IOS, shuttleIosAppId, "ABCDefgh");
    }
}
