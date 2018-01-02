package com.polidea.shuttle.infrastructure.mail;

import com.sun.jersey.multipart.FormDataMultiPart;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class NewUserNotificationMailService {

    private final static Logger LOGGER = getLogger(MailAuthService.class);

    private static final String NEW_USER_IN_SHUTTLE_EMAIL_TITLE = "New user in Shuttle: %s [%s]";
    private static final String NEW_USER_NOTIFICATION_EMAIL_TEMPLATE =
        "Hi, \n\n" +
            "We have new user created in Shuttle: %s [%s]\n\n" +
            "User creator: %s\n\n" +
            "Cheers,\n" +
            "Shuttle Team";

    private final MailService mailService;
    private final String mailDomain;

    public NewUserNotificationMailService(MailService mailService,
                                          String mailDomain) {
        this.mailService = mailService;
        this.mailDomain = mailDomain;
    }

    public void sendNotificationEmailAboutNewUser(String recipient,
                                                  String userCreatorEmail,
                                                  String newUserEmail,
                                                  String newUserName) {
        LOGGER.info("Sending notification email about new user to {}", recipient);
        mailService.sendEmail(createNewUserNotificationEmail(
            recipient,
            userCreatorEmail,
            newUserEmail,
            newUserName
        ));
    }

    private FormDataMultiPart createNewUserNotificationEmail(String recipient,
                                                             String userCreatorEmail,
                                                             String newUserEmail,
                                                             String newUserName) {
        FormDataMultiPart formData = new FormDataMultiPart();

        formData.field("to", recipient);
        formData.field("subject", format(NEW_USER_IN_SHUTTLE_EMAIL_TITLE,
                                         newUserEmail,
                                         newUserName));
        formData.field("text", format(NEW_USER_NOTIFICATION_EMAIL_TEMPLATE,
                                      newUserEmail,
                                      newUserName,
                                      userCreatorEmail));
        addAuthor(formData);
        return formData;
    }

    private void addAuthor(FormDataMultiPart formData) {
        formData.field("from", format("Shuttle <info@%s>", mailDomain));
    }
}
