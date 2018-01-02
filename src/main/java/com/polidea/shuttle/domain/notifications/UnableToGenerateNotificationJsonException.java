package com.polidea.shuttle.domain.notifications;

class UnableToGenerateNotificationJsonException extends RuntimeException {

    UnableToGenerateNotificationJsonException(Exception exception) {
        super("Failed to generate Notification JSON", exception);
    }

}


