package com.polidea.shuttle.domain.notifications;

public enum NotificationType {

    USER_ASSIGNED_TO_PROJECT(
        "notification_about_assignment_to_project_body"
    ),
    RELEASED_BUILD(
        "notification_about_released_build_body"
    ),
    PUBLISHED_BUILD(
        "notification_about_published_build_body"
    ),
    NEW_SHUTTLE_VERSION(
        "notification_about_new_shuttle_version_body"
    );

    private final String bodyKey;

    NotificationType(String bodyKey) {
        this.bodyKey = bodyKey;
    }

    public String bodyKey() {
        return bodyKey;
    }

}
