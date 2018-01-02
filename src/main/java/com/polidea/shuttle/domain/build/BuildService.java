package com.polidea.shuttle.domain.build;

import com.polidea.shuttle.domain.app.App;
import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.app.input.BuildRequest;
import com.polidea.shuttle.domain.build.output.AdminBuildListResponse;
import com.polidea.shuttle.domain.build.output.ClientBuildListResponse;
import com.polidea.shuttle.domain.build.output.factories.BuildListResponseFactory;
import com.polidea.shuttle.domain.shuttle.output.AdminShuttleBuildResponse;
import com.polidea.shuttle.domain.shuttle.output.AdminShuttleBuildsResponse;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.UserService;
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks;
import com.polidea.shuttle.infrastructure.web.UnableToGenerateBase64QrCodeImageException;
import com.polidea.shuttle.infrastructure.web.WebResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static com.polidea.shuttle.domain.app.Platform.ANDROID;
import static com.polidea.shuttle.domain.app.Platform.IOS;
import static java.nio.file.Files.readAllBytes;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;

@Service
@Transactional
public class BuildService {

    private final BuildListResponseFactory buildListResponseFactory;
    private final BuildRepository buildRepository;
    private final UserService userService;
    private final WebResources webResources;

    @Value("${shuttle.app-id.ios}")
    private String shuttleIosAppId;
    @Value("${shuttle.app-id.android}")
    private String shuttleAndroidAppId;

    @Autowired
    public BuildService(BuildRepository buildRepository,
                        UserService userService,
                        WebResources webResources,
                        PermissionChecks permissionChecks) {
        this.buildRepository = buildRepository;
        this.userService = userService;
        this.webResources = webResources;
        this.buildListResponseFactory = new BuildListResponseFactory(permissionChecks);
    }

    public void addBuild(BuildRequest buildRequest, App app) {
        User releaser = null;
        String releaserEmail = null;

        // TODO: I have removed try/catch clause because I got weird transaction exception from JPA
        // It is probably because exception was being thrown between two transactional services

//        try {
        releaser = userService.findUserWithoutException(buildRequest.getReleaserEmail());
//        } catch (UserNotFoundException exception) {
//            releaserEmail = buildRequest.getReleaserEmail();
//        }

        if (releaser == null) {
            releaserEmail = buildRequest.getReleaserEmail();
        }

        String buildIdentifier = buildRequest.getBuildIdentifier();
        Optional<Build> existingBuild = buildRepository.find(app.platform(), app.appId(), buildIdentifier);
        if (existingBuild.isPresent()) {
            throw new DuplicateBuildException(
                buildIdentifier,
                buildRequest.getVersion()
            );
        }
        buildRepository.createBuild(
            buildIdentifier,
            buildRequest.getVersion(),
            buildRequest.getReleaseNotes(),
            buildRequest.getHref(),
            buildRequest.getBytes(),
            app,
            releaser,
            releaserEmail
        );
    }

    public Build findLatestPublishedBuild(String appId, Platform platform) {
        return buildRepository.findNewestPublished(platform, appId)
                              .orElseThrow(() -> new BuildNotFoundException());
    }

    public AdminShuttleBuildsResponse findLatestPublishedShuttleBuildsForAdmin() {
        Optional<Build> androidBuild = buildRepository.findNewestPublished(ANDROID,
                                                                           shuttleAndroidAppId);
        Optional<Build> iosBuild = buildRepository.findNewestPublished(IOS,
                                                                       shuttleAndroidAppId);

        return new AdminShuttleBuildsResponse(
            createAdminShuttleBuildResponse(androidBuild),
            createAdminShuttleBuildResponse(iosBuild)
        );
    }

    public AdminShuttleBuildsResponse findLatestShuttleBuildsForAdmin() {
        Optional<Build> androidBuild = buildRepository.findNewest(ANDROID,
                                                                  shuttleAndroidAppId);
        Optional<Build> iosBuild = buildRepository.findNewest(IOS,
                                                              shuttleAndroidAppId);

        return new AdminShuttleBuildsResponse(
            createAdminShuttleBuildResponse(androidBuild),
            createAdminShuttleBuildResponse(iosBuild)
        );
    }

    public AdminBuildListResponse fetchAllAppBuilds(String appId, Platform platform) {
        Set<Build> builds = buildRepository.find(platform, appId);
        return buildListResponseFactory.createBuildListResponseForAdmin(platform, builds);
    }

    public AdminBuildListResponse fetchPublishedAppBuildsForAdmin(String appId, Platform platform) {
        Set<Build> builds = buildRepository.findPublished(platform, appId);
        return buildListResponseFactory.createBuildListResponseForAdmin(platform, builds);
    }

    public ClientBuildListResponse fetchAppBuildsForPublisher(String appId, Platform platform, String userEmail) {
        User user = findUser(userEmail);
        Set<Build> builds = buildRepository.find(platform, appId);
        return buildListResponseFactory.createBuildListResponseForClient(platform, builds, user);
    }

    public ClientBuildListResponse fetchPublishedAppBuilds(String appId, Platform platform, String userEmail) {
        User user = findUser(userEmail);
        Set<Build> builds = buildRepository.findPublished(platform, appId);
        return buildListResponseFactory.createBuildListResponseForClient(platform, builds, user);
    }

    public void deleteBuild(Platform platform, String appId, String buildIdentifier) {
        Build buildToDelete = findBuild(platform, appId, buildIdentifier);
        buildRepository.delete(buildToDelete);
    }

    public void publishBuild(Platform platform, String appId, String buildIdentifier) {
        Build build = findBuild(platform, appId, buildIdentifier);
        build.publish();
    }

    public void unpublishBuild(Platform platform, String appId, String buildIdentifier) {
        Build build = findBuild(platform, appId, buildIdentifier);
        build.unpublish();
    }

    public void favoriteBuild(String userEmail, Platform platform, String appId, String buildIdentifier) {
        User user = findUser(userEmail);
        Build build = findBuild(platform, appId, buildIdentifier);
        user.markBuildAsFavorite(build);
    }

    public void unfavoriteBuild(String userEmail, Platform platform, String appId, String buildIdentifier) {
        User user = findUser(userEmail);
        Build build = findBuild(platform, appId, buildIdentifier);
        user.markBuildAsNotFavorite(build);
    }

    public Build findBuild(Platform platform, String appId, String buildIdentifier) {
        return buildRepository.find(platform, appId, buildIdentifier)
                              .orElseThrow(() -> new BuildNotFoundException(buildIdentifier));
    }

    private AdminShuttleBuildResponse createAdminShuttleBuildResponse(Optional<Build> androidBuild) {
        return androidBuild.map(build -> {
            String href = build.href();

            try {
                File qrCode = webResources.androidQrCodeForHref(href);
                String qrCodeBase64 = encodeBase64String(readAllBytes(qrCode.toPath()));

                return new AdminShuttleBuildResponse(
                    qrCodeBase64,
                    build
                );
            } catch (IOException e) {
                throw new UnableToGenerateBase64QrCodeImageException(href);
            }
        }).orElse(null);
    }

    private User findUser(String userEmail) {
        return userService.findUser(userEmail);
    }

}
