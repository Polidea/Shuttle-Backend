package com.polidea.shuttle.domain.notifications.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.polidea.shuttle.domain.notifications.NotificationType;

import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

public class NotificationAboutPublishedBuild extends FirebaseNotification {

    public NotificationAboutPublishedBuild(Set<String> pushTokens,
                                           String buildVersionNumber,
                                           Integer projectId,
                                           String appId,
                                           String appName,
                                           String appIconHref,
                                           String androidIconResourceName,
                                           int pushTokensLimit) {
        super(
            pushTokens,
            NotificationType.PUBLISHED_BUILD,
            newArrayList(buildVersionNumber, appName),
            androidIconResourceName,
            new PublishedBuildData(projectId, appId, appName, appIconHref),
            pushTokensLimit
        );
    }

    @SuppressWarnings("WeakerAccess")
    private static class PublishedBuildData extends FirebaseNotification.Data {

        @JsonProperty("projectId")
        public final Integer projectId;
        @JsonProperty("appId")
        public final String appId;
        @JsonProperty("appName")
        public final String appName;
        @JsonProperty("appIconHref")
        public final String appIconHref;

        PublishedBuildData(Integer projectId, String appId, String appName, String appIconHref) {
            super("PublishedBuild");
            this.projectId = projectId;
            this.appId = appId;
            this.appName = appName;
            this.appIconHref = appIconHref;
        }

    }

}
