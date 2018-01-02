package com.polidea.shuttle.application;

import com.polidea.shuttle.domain.app.App;
import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.build.Build;
import com.polidea.shuttle.domain.build.BuildService;
import com.polidea.shuttle.domain.notifications.NotificationService;
import com.polidea.shuttle.domain.project.Project;
import com.polidea.shuttle.domain.user.User;
import org.junit.Test;

import static com.polidea.shuttle.domain.app.Platform.ANDROID;
import static com.polidea.shuttle.domain.app.Platform.IOS;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BuildPublisherTest {

    private final String shuttleIosAppId = "appId.of.shuttle.io";
    private final String shuttleAndroidAppId = "appId.of.shuttle.android";
    private final BuildService buildService = mock(BuildService.class);
    private final NotificationService notificationService = mock(NotificationService.class);

    private final Platform platform = ANDROID;
    private final String appId = "any.appId";
    private final String buildIdentifier = "any.buildIdentifier";

    @Test
    public void should_publish_Build() {
        // given
        App app = newApp(platform, appId);
        Build buildToPublish = newBuild(buildIdentifier, app);
        when(buildService.findBuild(platform, appId, buildIdentifier)).thenReturn(buildToPublish);
        BuildPublisher buildPublisher = newBuildPublisher();

        // when
        buildPublisher.publish(platform, appId, buildIdentifier);

        // then
        verify(buildService).publishBuild(
            platform,
            appId,
            buildIdentifier
        );
    }

    @Test
    public void should_notify_about_published_Build() {
        // given
        App app = newApp(platform, appId);
        Build buildToPublish = newBuild(buildIdentifier, app);
        when(buildService.findBuild(platform, appId, buildIdentifier)).thenReturn(buildToPublish);
        BuildPublisher buildPublisher = newBuildPublisher();

        // when
        buildPublisher.publish(platform, appId, buildIdentifier);

        // then
        verify(notificationService).notifyAboutPublishedBuild(
            platform,
            appId,
            buildIdentifier
        );
    }

    @Test
    public void should_notify_about_new_iOS_Shuttle_version_if_given_Build_is_for_Shuttle() {
        // given
        App app = newApp(IOS, shuttleIosAppId);
        Build buildToPublish = newBuild("any.build.123", app);
        when(buildService.findBuild(IOS, shuttleIosAppId, "any.build.123")).thenReturn(buildToPublish);
        BuildPublisher buildPublisher = newBuildPublisher();

        // when
        buildPublisher.publish(IOS, shuttleIosAppId, "any.build.123");

        // then
        verify(notificationService).notifyAboutNewShuttleVersion(
            IOS,
            shuttleIosAppId,
            "any.build.123"
        );
    }

    @Test
    public void should_notify_about_new_Android_Shuttle_version_if_given_Build_is_for_Shuttle() {
        // given
        App app = newApp(ANDROID, shuttleAndroidAppId);
        Build buildToPublish = newBuild("any.build.123", app);
        when(buildService.findBuild(ANDROID, shuttleAndroidAppId, "any.build.123")).thenReturn(buildToPublish);
        BuildPublisher buildPublisher = newBuildPublisher();

        // when
        buildPublisher.publish(ANDROID, shuttleAndroidAppId, "any.build.123");

        // then
        verify(notificationService).notifyAboutNewShuttleVersion(
            ANDROID,
            shuttleAndroidAppId,
            "any.build.123"
        );
    }

    @Test
    public void should_notify_about_new_Shuttle_version_if_given_Build_is_newer_than_other_published() {
        // given
        App app = newApp(IOS, shuttleIosAppId);
        Build buildToPublish = newBuild("build.to.publish", app);
        Build alreadyPublishedBuild = newBuild("already.published.build", app);
        app.setRawBuilds(asList(alreadyPublishedBuild, buildToPublish));
        when(buildService.findBuild(IOS, shuttleIosAppId, "build.to.publish")).thenReturn(buildToPublish);
        BuildPublisher buildPublisher = newBuildPublisher();

        // and given
        buildToPublish.setReleaseDate(51);
        alreadyPublishedBuild.publish();
        alreadyPublishedBuild.setReleaseDate(50);

        // when
        buildPublisher.publish(IOS, shuttleIosAppId, "build.to.publish");

        // then
        verify(notificationService).notifyAboutNewShuttleVersion(
            IOS,
            shuttleIosAppId,
            "build.to.publish"
        );
    }

    @Test
    public void should_notify_about_new_Shuttle_version_if_given_Build_is_from_same_moment_as_other_published() {
        // given
        App app = newApp(IOS, shuttleIosAppId);
        Build buildToPublish = newBuild("build.to.publish", app);
        Build olderPublishedBuild = newBuild("older.published.build", app);
        app.setRawBuilds(asList(olderPublishedBuild, buildToPublish));
        when(buildService.findBuild(IOS, shuttleIosAppId, "build.to.publish")).thenReturn(buildToPublish);
        BuildPublisher buildPublisher = newBuildPublisher();

        // and given
        buildToPublish.setReleaseDate(50);
        olderPublishedBuild.publish();
        olderPublishedBuild.setReleaseDate(50);

        // when
        buildPublisher.publish(IOS, shuttleIosAppId, "build.to.publish");

        // then
        verify(notificationService).notifyAboutNewShuttleVersion(
            IOS,
            shuttleIosAppId,
            "build.to.publish"
        );
    }

    @Test
    public void should_notify_about_new_Shuttle_version_if_given_Build_is_newer_than_other_unpublished() {
        // given
        App app = newApp(IOS, shuttleIosAppId);
        Build buildToPublish = newBuild("build.to.publish", app);
        Build otherBuild = newBuild("already.published.build", app);
        app.setRawBuilds(asList(otherBuild, buildToPublish));
        when(buildService.findBuild(IOS, shuttleIosAppId, "build.to.publish")).thenReturn(buildToPublish);
        BuildPublisher buildPublisher = newBuildPublisher();

        // and given
        buildToPublish.setReleaseDate(50);
        otherBuild.setReleaseDate(51);

        // when
        buildPublisher.publish(IOS, shuttleIosAppId, "build.to.publish");

        // then
        verify(notificationService).notifyAboutNewShuttleVersion(
            IOS,
            shuttleIosAppId,
            "build.to.publish"
        );
    }

    @Test
    public void should_not_notify_about_new_Shuttle_version_if_given_Build_is_older_than_other_published() {
        // given
        App app = newApp(IOS, shuttleIosAppId);
        Build buildToPublish = newBuild("build.to.publish", app);
        Build newerPublishedBuild = newBuild("newer.published.build", app);
        app.setRawBuilds(asList(newerPublishedBuild, buildToPublish));
        when(buildService.findBuild(IOS, shuttleIosAppId, "build.to.publish")).thenReturn(buildToPublish);
        BuildPublisher buildPublisher = newBuildPublisher();

        // and given
        buildToPublish.setReleaseDate(50);
        newerPublishedBuild.publish();
        newerPublishedBuild.setReleaseDate(51);

        // when
        buildPublisher.publish(IOS, shuttleIosAppId, "build.to.publish");

        // then
        verify(notificationService, never()).notifyAboutNewShuttleVersion(
            anyPlatform(),
            anyAppId(),
            anyBuildIdentifier()
        );
    }

    @Test
    public void should_not_notify_about_new_Shuttle_version_if_given_Build_is_already_published() {
        // given
        App app = newApp(IOS, shuttleIosAppId);
        Build buildToPublish = newBuild("build.to.publish", app);
        app.setRawBuilds(asList(buildToPublish));
        when(buildService.findBuild(IOS, shuttleIosAppId, "build.to.publish")).thenReturn(buildToPublish);
        BuildPublisher buildPublisher = newBuildPublisher();

        // and given
        buildToPublish.publish();
        buildToPublish.setReleaseDate(123);

        // when
        buildPublisher.publish(IOS, shuttleIosAppId, "build.to.publish");

        // then
        verify(notificationService, never()).notifyAboutNewShuttleVersion(
            anyPlatform(),
            anyAppId(),
            anyBuildIdentifier()
        );
    }

    @Test
    public void should_not_notify_about_new_Shuttle_version_if_given_Build_is_for_different_platform() {
        // given
        App app = newApp(ANDROID, shuttleIosAppId);
        Build buildToPublish = newBuild("any.build.123", app);
        when(buildService.findBuild(ANDROID, shuttleIosAppId, "any.build.123")).thenReturn(buildToPublish);
        BuildPublisher buildPublisher = newBuildPublisher();

        // when
        buildPublisher.publish(ANDROID, shuttleIosAppId, "any.build.123");

        // then
        verify(notificationService, never()).notifyAboutNewShuttleVersion(
            anyPlatform(),
            anyAppId(),
            anyBuildIdentifier()
        );
    }

    private Project anyProject() {
        return new Project("Any Project");
    }

    private Platform anyPlatform() {
        return any(Platform.class);
    }

    private App newApp(Platform platform, String appId) {
        return new App(anyProject(), platform, appId, anyAppName());
    }

    private String anyAppId() {
        return anyString();
    }

    private String anyAppName() {
        return "Any App";
    }

    private Build newBuild(String buildIdentifier, App app) {
        return new Build(
            buildIdentifier,
            "any.version.number",
            "Any Release Notes",
            "http://any.href",
            anyByteCount(),
            app,
            anyReleaser(),
            "any.releaser@email"
        );
    }

    private String anyBuildIdentifier() {
        return anyString();
    }

    private long anyByteCount() {
        return 123L;
    }

    private User anyReleaser() {
        return new User(
            "any@email",
            "Any Name"
        );
    }

    private BuildPublisher newBuildPublisher() {
        return new BuildPublisher(
            shuttleIosAppId,
            shuttleAndroidAppId,
            buildService,
            notificationService
        );
    }
}
