package com.polidea.shuttle.application;

import com.polidea.shuttle.domain.app.App;
import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.build.Build;
import com.polidea.shuttle.domain.build.BuildService;
import com.polidea.shuttle.domain.notifications.NotificationService;

import java.util.OptionalLong;

import static com.polidea.shuttle.domain.app.Platform.ANDROID;
import static com.polidea.shuttle.domain.app.Platform.IOS;

public class BuildPublisher {

    private final String shuttleIosAppId;
    private final String shuttleAndroidAppId;
    private final BuildService buildService;
    private final NotificationService notificationService;

    public BuildPublisher(String shuttleIosAppId,
                          String shuttleAndroidAppId,
                          BuildService buildService,
                          NotificationService notificationService) {
        this.shuttleIosAppId = shuttleIosAppId;
        this.shuttleAndroidAppId = shuttleAndroidAppId;
        this.buildService = buildService;
        this.notificationService = notificationService;
    }

    public void publish(Platform platform, String appId, String buildIdentifier) {
        buildService.publishBuild(platform, appId, buildIdentifier);
        notificationService.notifyAboutPublishedBuild(platform, appId, buildIdentifier);

        Build build = buildService.findBuild(platform, appId, buildIdentifier);
        if (isNewestUnpublishedShuttleBuild(build)) {
            notificationService.notifyAboutNewShuttleVersion(platform, appId, buildIdentifier);
        }

    }

    private boolean isNewestUnpublishedShuttleBuild(Build build) {
        if (build.isPublished()) {
            return false;
        }

        App app = build.app();
        if (!isShuttle(app.platform(), app.appId())) {
            return false;
        }

        OptionalLong newestPublishedBuildReleaseDate = app.lastPublishedReleaseDate();
        if (newestPublishedBuildReleaseDate.isPresent()) {
            return newestPublishedBuildReleaseDate.getAsLong() <= build.releaseDate();
        }
        return true;
    }

    private boolean isShuttle(Platform platform, String appId) {
        return (platform == ANDROID && appId.equals(shuttleAndroidAppId)) ||
            (platform == IOS && appId.equals(shuttleIosAppId));
    }
}
