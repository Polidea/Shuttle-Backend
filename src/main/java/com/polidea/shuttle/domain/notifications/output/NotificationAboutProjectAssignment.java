package com.polidea.shuttle.domain.notifications.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.polidea.shuttle.domain.notifications.NotificationType;

import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

public class NotificationAboutProjectAssignment extends FirebaseNotification {

    public NotificationAboutProjectAssignment(Set<String> pushTokens,
                                              Integer projectId,
                                              String projectName,
                                              String androidIconResourceName,
                                              int pushTokensLimit) {
        super(
            pushTokens,
            NotificationType.USER_ASSIGNED_TO_PROJECT,
            newArrayList(projectName),
            androidIconResourceName,
            new ProjectAssignmentData(projectId, projectName),
            pushTokensLimit
        );
    }

    @SuppressWarnings("WeakerAccess")
    private static class ProjectAssignmentData extends FirebaseNotification.Data {

        @JsonProperty("projectId")
        public final Integer projectId;
        @JsonProperty("projectName")
        public final String projectName;

        ProjectAssignmentData(Integer projectId, String projectName) {
            super("AssignedToProject");
            this.projectId = projectId;
            this.projectName = projectName;
        }

    }

}
