package com.polidea.shuttle.domain.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.polidea.shuttle.domain.notifications.output.FirebaseNotification;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.slf4j.Logger;

import javax.ws.rs.core.MediaType;

import static org.slf4j.LoggerFactory.getLogger;

public class NotificationsSenderService {

    private final WebResource webResource;
    private final String authorizationKey;

    private static final Logger LOGGER = getLogger(NotificationsSenderService.class);

    public NotificationsSenderService(WebResource webResource, String authorizationKey) {
        this.webResource = webResource;
        this.authorizationKey = authorizationKey;
    }

    public void send(FirebaseNotification firebaseNotification) throws JsonProcessingException {
        String firebaseNotificationJson = objectWriter().writeValueAsString(firebaseNotification);
        new Thread(() -> {
            ClientResponse firebaseResponse = webResource.type(MediaType.APPLICATION_JSON_TYPE)
                                                         .header("Authorization", authorizationKey)
                                                         .post(ClientResponse.class, firebaseNotificationJson);
            String body = firebaseResponse.getEntity(String.class);
            int status = firebaseResponse.getStatus();

            if (status != 200) {
                LOGGER.warn("Firebase notification response:\n statusCode: {}, body: {}", status, body);
            }
        }).start();
    }

    private ObjectWriter objectWriter() {
        return new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

}
