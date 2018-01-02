package com.polidea.shuttle.domain.notifications.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.polidea.shuttle.domain.notifications.NotificationType;

import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

public class NotificationAboutReleasedBuild extends FirebaseNotification {

    public NotificationAboutReleasedBuild(Set<String> pushTokens,
                                          String buildVersionNumber,
                                          Integer projectId,
                                          String appId,
                                          String appName,
                                          String appIconHref,
                                          String buildHref,
                                          String androidIconResourceName,
                                          int pushTokensLimit) {
        super(
            pushTokens,
            NotificationType.RELEASED_BUILD,
            newArrayList(buildVersionNumber, appName),
            androidIconResourceName,
            new ReleasedBuildData(projectId, appId, appName, appIconHref, buildHref),
            pushTokensLimit
        );
    }

    @SuppressWarnings("WeakerAccess")
    private static class ReleasedBuildData extends FirebaseNotification.Data {

        @JsonProperty("projectId")
        public final Integer projectId;
        @JsonProperty("appId")
        public final String appId;
        @JsonProperty("appName")
        public final String appName;
        @JsonProperty("appIconHref")
        public final String appIconHref;
        @JsonProperty("buildHref")
        public final String buildHref;

        ReleasedBuildData(Integer projectId, String appId, String appName, String appIconHref, String buildHref) {
            super("ReleasedBuild");
            this.projectId = projectId;
            this.appId = appId;
            this.appName = appName;
            this.appIconHref = appIconHref;
            this.buildHref = buildHref;
        }

    }

}
