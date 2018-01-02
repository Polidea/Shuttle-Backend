package com.polidea.shuttle.domain.notifications.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.polidea.shuttle.domain.notifications.NotificationType;
import com.polidea.shuttle.error_codes.ErrorCode;
import com.polidea.shuttle.error_codes.InternalServerErrorException;

import java.util.List;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
public abstract class FirebaseNotification {

    @JsonProperty("registration_ids")
    public final Set<String> registrationIds;
    @JsonProperty("notification")
    public final NotificationDetails notificationDetails;
    @JsonProperty("data")
    public final Data customData;

    FirebaseNotification(Set<String> pushTokens,
                         NotificationType type,
                         List<String> messageBodyArguments,
                         String androidIconResourceName,
                         Data customData,
                         int pushTokensLimit) {
        if (pushTokens.size() > pushTokensLimit) {
            throw new TooManyPushTokensInBatchException();
        }
        this.registrationIds = pushTokens;
        this.notificationDetails = new NotificationDetails(type, messageBodyArguments, androidIconResourceName);
        this.customData = customData;
    }

    @SuppressWarnings("WeakerAccess")
    private static class NotificationDetails {

        @JsonProperty("body_loc_key")
        public final String bodyKey;
        @JsonProperty("body_loc_args")
        public final List<String> bodyArguments;
        @JsonProperty("icon")
        public final String androidIconResourceName;

        private NotificationDetails(NotificationType type, List<String> messageBodyArguments, String androidIconResourceName) {
            this.bodyKey = type.bodyKey();
            this.bodyArguments = messageBodyArguments;
            this.androidIconResourceName = androidIconResourceName;
        }

    }

    public class TooManyPushTokensInBatchException extends InternalServerErrorException {
        public TooManyPushTokensInBatchException() {
            super("Trying to send too many push notifications at once", ErrorCode.TOO_MANY_PUSH_TOKENS);
        }
    }

    static abstract class Data {

        @JsonProperty("notificationType")
        public final String notificationType;

        protected Data(String notificationType) {
            this.notificationType = notificationType;
        }
    }

}
