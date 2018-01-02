package com.polidea.shuttle.configuration;

import com.polidea.shuttle.domain.notifications.NotificationsSenderService;
import com.sun.jersey.api.client.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SuppressWarnings("unused")
public class NotificationConfiguration {

    @Value("${shuttle.fcm.url}")
    private String fcmUrl;

    @Value("${shuttle.fcm.key}")
    private String fcmKey;

    @Bean
    public NotificationsSenderService notificationsSenderService() {
        return new NotificationsSenderService(
            Client.create().resource(fcmUrl),
            fcmKey
        );
    }

}
