package com.polidea.shuttle.infrastructure.web;

import com.polidea.shuttle.infrastructure.mail.UnableToGenerateQRCodeException;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

import static com.polidea.shuttle.infrastructure.qr_codes.QrCodes.generateQrCode;
import static java.lang.String.format;
import static net.glxn.qrgen.core.image.ImageType.PNG;

@Component
public class WebResources {

    @Value("${shuttle.qrcode.width}")
    private Integer qrCodeWidth;
    @Value("${shuttle.qrcode.height}")
    private Integer qrCodeHeight;

    private final File polideaLogo;
    private final File landingPageHeader;
    private final File facebookIcon;
    private final File twitterIcon;
    private final File githubIcon;

    public WebResources() throws IOException {
        polideaLogo = new ClassPathResource("images/polidea-logo.png").getFile();
        landingPageHeader = new ClassPathResource("images/shuttle-invitation-mail-header.png").getFile();
        facebookIcon = new ClassPathResource("images/polidea-facebook-icon.png").getFile();
        twitterIcon = new ClassPathResource("images/polidea-twitter-icon.png").getFile();
        githubIcon = new ClassPathResource("images/polidea-github-icon.png").getFile();
    }

    public File polideaLogo() {
        return polideaLogo;
    }

    public File landingPageHeader() {
        return landingPageHeader;
    }

    public File facebookIcon() {
        return facebookIcon;
    }

    public File githubIcon() {
        return githubIcon;
    }

    public File twitterIcon() {
        return twitterIcon;
    }

    public File iosQrCodeForHref(String href) throws UnableToGenerateQRCodeException {
        // Base64 encoded href is appended in order to prevent browser caching.
        return generateQrCode(href,
                              format("ios-qr-code-%s.png", hashOf(href)),
                              PNG,
                              qrCodeWidth,
                              qrCodeHeight);
    }

    public File androidQrCodeForHref(String href) throws UnableToGenerateQRCodeException {
        // Base64 encoded href is appended in order to prevent browser caching.
        return generateQrCode(href,
                              format("android-qr-code-%s.png", hashOf(href)),
                              PNG,
                              qrCodeWidth,
                              qrCodeHeight);
    }

    private String hashOf(String url) {
        return DigestUtils.md5Hex(url);
    }

}
