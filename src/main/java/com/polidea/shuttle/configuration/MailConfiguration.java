package com.polidea.shuttle.configuration;

import com.polidea.shuttle.domain.build.BuildService;
import com.polidea.shuttle.domain.user.permissions.global.GlobalPermissionsService;
import com.polidea.shuttle.infrastructure.DownloadUrls;
import com.polidea.shuttle.infrastructure.mail.MailAuthService;
import com.polidea.shuttle.infrastructure.mail.MailResources;
import com.polidea.shuttle.infrastructure.mail.MailService;
import com.polidea.shuttle.infrastructure.mail.NewUserNotificationMailService;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.String.format;

@Configuration
@SuppressWarnings("unused")
public class MailConfiguration {

    @Value("${shuttle.mailgun.domain}")
    private String mailgunDomain;

    @Value("${shuttle.mailgun.key}")
    private String mailgunKey;

    @Autowired
    private MailResources mailResources;

    @Autowired
    private WebResource mailSendingEndpoint;

    @Bean
    public WebResource webResource() {
        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter("api", mailgunKey));
        return client.resource(format("https://api.mailgun.net/v3/%s/messages", mailgunDomain));
    }

    @Bean
    public MailService mailService() {
        return new MailService(mailSendingEndpoint);
    }

    @Bean
    public MailAuthService mailAuthService(MailService mailService,
                                           BuildService buildService,
                                           GlobalPermissionsService globalPermissionsService,
                                           DownloadUrls downloadUrls) {
        return new MailAuthService(
            mailService,
            buildService,
            mailResources,
            mailgunDomain,
            downloadUrls
        );
    }

    @Bean
    public NewUserNotificationMailService newUserNotificationMailService(MailService mailService) {
        return new NewUserNotificationMailService(
            mailService,
            mailgunDomain
        );
    }

}

