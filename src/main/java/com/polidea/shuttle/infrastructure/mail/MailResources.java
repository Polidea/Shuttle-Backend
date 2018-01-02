package com.polidea.shuttle.infrastructure.mail;

import com.google.common.io.Files;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

import static com.polidea.shuttle.infrastructure.qr_codes.QrCodes.generateQrCode;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static net.glxn.qrgen.core.image.ImageType.PNG;

@Component
public class MailResources {

    @Value("${shuttle.qrcode.width}")
    private Integer qrCodeWidth;
    @Value("${shuttle.qrcode.height}")
    private Integer qrCodeHeight;

    private final File facebookIcon;
    private final File twitterIcon;
    private final File githubIcon;
    private final File invitationMailHeader;
    private final File authorizationMailHeader;
    private final File polideaLogo;
    private final String invitationHtmlTemplate;
    private final String activationHtmlTemplate;

    public MailResources() throws IOException {
        facebookIcon = new ClassPathResource("images/polidea-facebook-icon.png").getFile();
        twitterIcon = new ClassPathResource("images/polidea-twitter-icon.png").getFile();
        githubIcon = new ClassPathResource("images/polidea-github-icon.png").getFile();
        invitationMailHeader = new ClassPathResource("images/shuttle-invitation-mail-header.png").getFile();
        authorizationMailHeader = new ClassPathResource("images/shuttle-authorization-mail-header.png").getFile();
        polideaLogo = new ClassPathResource("images/polidea-logo.png").getFile();
        invitationHtmlTemplate = Files.toString(
            new ClassPathResource("mail/invitation_inlined.html").getFile(),
            defaultCharset()
        );
        activationHtmlTemplate = Files.toString(
            new ClassPathResource("mail/access_code_claim_email_inlined.html").getFile(),
            defaultCharset()
        );
    }

    public File getFacebookIcon() {
        return facebookIcon;
    }

    public File getQrCodeAndroid(String href) throws UnableToGenerateQRCodeException {
        String hashHref = hashOf(href);
        // Here we needed to add some way of creating distinguished file names between different versions of builds
        // Because in some email browsers, files from past emails are being cached
        return generateQrCode(href, format("android-qr-code-%s.png", hashHref), PNG, qrCodeWidth, qrCodeHeight);
    }

    public File getQrCodeIOS(String href) throws UnableToGenerateQRCodeException {
        String hashHref = hashOf(href);
        // Here we needed to add some way of creating distinguished file names between different versions of builds
        // Because in some email browsers, files from past emails are being cached
        return generateQrCode(href, format("ios-qr-code-%s.png", hashHref), PNG, qrCodeWidth, qrCodeHeight);
    }

    private String hashOf(String url) {
        return DigestUtils.md5Hex(url);
    }

    public File getGithubIcon() {
        return githubIcon;
    }

    public String getInvitationHtmlTemplate() {
        return invitationHtmlTemplate;
    }

    public File getPolideaLogo() {
        return polideaLogo;
    }

    public File getInvitationMailHeader() {
        return invitationMailHeader;
    }

    public File getAuthorizationMailHeader() {
        return authorizationMailHeader;
    }

    public File getTwitterIcon() {
        return twitterIcon;
    }

    public String getActivationHtmlTemplate() {
        return activationHtmlTemplate;
    }
}
