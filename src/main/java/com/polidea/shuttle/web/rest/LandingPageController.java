package com.polidea.shuttle.web.rest;

import com.polidea.shuttle.domain.build.BuildService;
import com.polidea.shuttle.infrastructure.DownloadUrls;
import com.polidea.shuttle.infrastructure.web.WebResources;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.IOException;

import static com.polidea.shuttle.domain.app.Platform.ANDROID;
import static com.polidea.shuttle.domain.app.Platform.IOS;
import static java.nio.file.Files.readAllBytes;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@SuppressWarnings("unused")
@Controller
public class LandingPageController {

    @Value("${shuttle.app-id.ios}")
    private String shuttleIosAppId;
    @Value("${shuttle.app-id.android}")
    private String shuttleAndroidAppId;

    private final BuildService buildService;
    private final DownloadUrls downloadUrls;
    private final WebResources webResources;

    public LandingPageController(BuildService buildService,
                                 DownloadUrls downloadUrls,
                                 WebResources webResources) {
        this.buildService = buildService;
        this.downloadUrls = downloadUrls;
        this.webResources = webResources;
    }

    public static final String LANDING_PAGE_PATH = "/";

    @RequestMapping(method = GET, value = LANDING_PAGE_PATH)
    public String getAuthRedirectPage(Model landingPageAttributes) throws IOException {

        String androidHref = buildService.findLatestPublishedBuild(shuttleAndroidAppId, ANDROID).href();
        String iosHref = buildService.findLatestPublishedBuild(shuttleIosAppId, IOS).href();

        landingPageAttributes.addAttribute(
            "android_url",
            downloadUrls.createAndroidDownloadUrlForStoreHref(androidHref)
        );
        landingPageAttributes.addAttribute(
            "ios_url",
            downloadUrls.createIosDownloadUrlForStoreHref(iosHref)
        );

        File androidQrCode = webResources.androidQrCodeForHref(androidHref);
        File iosQrCode = webResources.iosQrCodeForHref(iosHref);

        landingPageAttributes.addAttribute(
            "android_qr_code_base64",
            encodeBase64String(readAllBytes(androidQrCode.toPath()))
        );
        landingPageAttributes.addAttribute(
            "ios_qr_code_base64",
            encodeBase64String(readAllBytes(iosQrCode.toPath()))
        );

        File polideaLogo = webResources.polideaLogo();
        File headerImage = webResources.landingPageHeader();
        File facebookIcon = webResources.facebookIcon();
        File twitterIcon = webResources.twitterIcon();
        File githubIcon = webResources.githubIcon();

        landingPageAttributes.addAttribute(
            "polidea_logo_base64",
            encodeBase64String(readAllBytes(polideaLogo.toPath()))
        );
        landingPageAttributes.addAttribute(
            "header_image_base64",
            encodeBase64String(readAllBytes(headerImage.toPath()))
        );
        landingPageAttributes.addAttribute(
            "facebook_icon_base64",
            encodeBase64String(readAllBytes(facebookIcon.toPath()))
        );
        landingPageAttributes.addAttribute(
            "twitter_icon_base64",
            encodeBase64String(readAllBytes(twitterIcon.toPath()))
        );
        landingPageAttributes.addAttribute(
            "github_icon_base64",
            encodeBase64String(readAllBytes(githubIcon.toPath()))
        );

        return "landing_page";
    }

}

