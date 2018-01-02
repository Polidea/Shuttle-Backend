package com.polidea.shuttle.infrastructure.mail;

import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.build.Build;
import com.polidea.shuttle.domain.build.BuildService;
import com.polidea.shuttle.infrastructure.DownloadUrls;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.Arrays;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class MailAuthService {

    private final static Logger LOGGER = getLogger(MailAuthService.class);

    @Value("${shuttle.host:}")
    private String host;
    @Value("${shuttle.app-id.ios}")
    private String shuttleIosAppId;
    @Value("${shuttle.app-id.android}")
    private String shuttleAndroidAppId;

    private final MailService mailService;
    private final BuildService buildService;
    private final MailResources resources;
    private final String mailDomain;
    private final DownloadUrls downloadUrls;

    public MailAuthService(MailService mailService,
                           BuildService buildService,
                           MailResources mailResources,
                           String mailDomain,
                           DownloadUrls downloadUrls) {
        this.mailService = mailService;
        this.buildService = buildService;
        this.resources = mailResources;
        this.mailDomain = mailDomain;
        this.downloadUrls = downloadUrls;
    }

    public void sendVerificationCode(String recipient, String verificationCode) {
        mailService.sendEmail(createAuthMessage(recipient, verificationCode));
    }

    private FormDataMultiPart createAuthMessage(String recipient, String verificationCode) {
        FormDataMultiPart formData = new FormDataMultiPart();
        formData.field("to", recipient);
        formData.field("subject", "Shuttle access code");
        formData.field("html", getTemplateWithAttachedCode(verificationCode, recipient));
        addAuthor(formData);
        insertInlineResources(formData, resources.getAuthorizationMailHeader());
        return formData;
    }

    private String getTemplateWithAttachedCode(String verificationCode, String email) {
        String loginUrl = format("%s/auth/redirect?email=%s&code=%s",
                                 this.host,
                                 email,
                                 verificationCode);
        String activationHtmlTemplate = resources.getActivationHtmlTemplate();
        activationHtmlTemplate = activationHtmlTemplate.replace("$SECURE_CODE", verificationCode);
        activationHtmlTemplate = activationHtmlTemplate.replace("$login_url", loginUrl);
        return activationHtmlTemplate;
    }

    public void sendInvitationEmail(String recipient) throws UnableToGenerateQRCodeException {
        LOGGER.info("Sending invitation email to {}", recipient);
        Build androidStoreBuild = buildService.findLatestPublishedBuild(shuttleAndroidAppId, Platform.ANDROID);
        Build iOSStoreBuild = buildService.findLatestPublishedBuild(shuttleIosAppId, Platform.IOS);
        mailService.sendEmail(createInvitationMessage(
            recipient,
            androidStoreBuild.href(),
            iOSStoreBuild.href()
        ));
    }

    private FormDataMultiPart createInvitationMessage(String recipient, String hrefStoreAndroid, String hrefStoreIOS) throws UnableToGenerateQRCodeException {
        String androidUrl = downloadUrls.createAndroidDownloadUrlForStoreHref(hrefStoreAndroid);
        String iosUrl = downloadUrls.createIosDownloadUrlForStoreHref(hrefStoreIOS);
        String invitationHtmlTemplate = resources.getInvitationHtmlTemplate();
        invitationHtmlTemplate = invitationHtmlTemplate.replace("$android_url", androidUrl);
        invitationHtmlTemplate = invitationHtmlTemplate.replace("$ios_url", iosUrl);

        File fileQrCodeAndroid = resources.getQrCodeAndroid(hrefStoreAndroid);
        File fileQrCodeIOS = resources.getQrCodeIOS(hrefStoreIOS);

        invitationHtmlTemplate = invitationHtmlTemplate.replace("$ios_qr_code_filename", fileQrCodeIOS.getName());
        invitationHtmlTemplate = invitationHtmlTemplate.replace("$android_qr_code_filename", fileQrCodeAndroid.getName());

        FormDataMultiPart formData = new FormDataMultiPart();

        formData.field("to", recipient);
        formData.field("subject", "Welcome to Shuttle!");
        formData.field("html", invitationHtmlTemplate);

        addAuthor(formData);
        insertInlineResources(
            formData,
            resources.getFacebookIcon(),
            resources.getGithubIcon(),
            resources.getTwitterIcon(),
            resources.getInvitationMailHeader(),
            resources.getPolideaLogo(),
            fileQrCodeAndroid,
            fileQrCodeIOS
        );
        return formData;
    }

    private void addAuthor(FormDataMultiPart formData) {
        formData.field("from", format("Shuttle <info@%s>", mailDomain));
    }

    private void insertInlineResources(FormDataMultiPart formData, File... filesToInline) {
        Arrays.stream(filesToInline)
              .forEach(file -> formData.bodyPart(new FileDataBodyPart("inline", file, MediaType.APPLICATION_OCTET_STREAM_TYPE)));
    }
}

