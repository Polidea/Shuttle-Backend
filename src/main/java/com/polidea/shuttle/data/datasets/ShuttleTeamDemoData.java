package com.polidea.shuttle.data.datasets;

import com.polidea.shuttle.data.DataLoadHelper;
import com.polidea.shuttle.domain.user.access_token.TokenType;
import org.slf4j.Logger;

import static com.google.common.collect.Lists.newArrayList;
import static com.polidea.shuttle.domain.app.Platform.ANDROID;
import static com.polidea.shuttle.domain.app.Platform.IOS;
import static com.polidea.shuttle.domain.user.permissions.PermissionType.ADMIN;
import static com.polidea.shuttle.domain.user.permissions.PermissionType.ARCHIVER;
import static com.polidea.shuttle.domain.user.permissions.PermissionType.MUTER;
import static com.polidea.shuttle.domain.user.permissions.PermissionType.PUBLISHER;
import static org.slf4j.LoggerFactory.getLogger;

public class ShuttleTeamDemoData {

    private static final Logger LOGGER = getLogger(ShuttleTeamDemoData.class);

    private final DataLoadHelper dataLoadHelper;

    public ShuttleTeamDemoData(DataLoadHelper dataLoadHelper) {
        this.dataLoadHelper = dataLoadHelper;
    }

    public void load() {
        LOGGER.info("Loading Shuttle Team data...");

        String shuttleTeamUserEmail = "shuttle-team@shuttle-test.com";

        dataLoadHelper.createUserIfMissing(
            shuttleTeamUserEmail,
            "Shuttle Team Demo User"
        );
        dataLoadHelper.createOrRenewAccessToken(
            shuttleTeamUserEmail,
            TokenType.CLIENT,
            "shuttle-team-token",
            "shuttle-team-device"
        );

        int projectDemo1Id = createProjectDemo1(shuttleTeamUserEmail);
        int projectDemo2Id = createProjectDemo2(shuttleTeamUserEmail);
        int projectWithoutBuildsId = createProjectWithoutBuilds();
        int projectWithoutAppsId = createProjectWithoutApps();

        dataLoadHelper.assignUserToProject(shuttleTeamUserEmail, projectDemo1Id);
        dataLoadHelper.assignUserToProject(shuttleTeamUserEmail, projectDemo2Id);
        dataLoadHelper.assignUserToProject(shuttleTeamUserEmail, projectWithoutBuildsId);
        dataLoadHelper.assignUserToProject(shuttleTeamUserEmail, projectWithoutAppsId);

        dataLoadHelper.setGlobalPermissions(shuttleTeamUserEmail, ADMIN);
        dataLoadHelper.setGlobalPermissions(shuttleTeamUserEmail, MUTER);
        dataLoadHelper.setGlobalPermissions(shuttleTeamUserEmail, PUBLISHER);
        dataLoadHelper.setProjectPermissions(
            shuttleTeamUserEmail,
            projectDemo2Id,
            newArrayList(MUTER, ARCHIVER, PUBLISHER)
        );
        dataLoadHelper.setProjectPermissions(
            shuttleTeamUserEmail,
            projectWithoutBuildsId,
            newArrayList(MUTER, ARCHIVER, PUBLISHER)
        );
        dataLoadHelper.setProjectPermissions(
            shuttleTeamUserEmail,
            projectWithoutAppsId,
            newArrayList(MUTER, ARCHIVER, PUBLISHER)
        );

        dataLoadHelper.createUserIfMissing("member1@shuttle-test.com", "First Member");
        dataLoadHelper.createUserIfMissing("member2@shuttle-test.com", "Second Member");
        dataLoadHelper.createUserIfMissing("member3@shuttle-test.com", "Third Member");
        dataLoadHelper.createUserIfMissing("member4@shuttle-test.com", "Fourth Member");

        dataLoadHelper.addMemberToProject("member1@shuttle-test.com", projectDemo1Id);

        dataLoadHelper.addMemberToProject("member1@shuttle-test.com", projectDemo2Id);
        dataLoadHelper.addMemberToProject("member2@shuttle-test.com", projectDemo2Id);
        dataLoadHelper.addMemberToProject("member3@shuttle-test.com", projectDemo2Id);
        dataLoadHelper.addMemberToProject("member4@shuttle-test.com", projectDemo2Id);
    }

    private int createProjectDemo1(String buildsReleaserEmail) {
        int projectDemo1Id = dataLoadHelper.createProjectIfMissing("Project Demo 1");

        dataLoadHelper.createAppIfMissing(projectDemo1Id, IOS, "eu.testapp", "Test App iOS");
        dataLoadHelper.createAppIfMissing(projectDemo1Id, ANDROID, "eu.testapp", "Test App Android");
        dataLoadHelper.createAppIfMissing(projectDemo1Id, IOS, "eu.testapp2", "Test App 2 iOS");
        dataLoadHelper.createAppIfMissing(projectDemo1Id, ANDROID, "eu.testapp2", "Test App 2 Android");

        dataLoadHelper.createBuildIfMissing(
            IOS, "eu.testapp", "ABCDefgh1", "0.21.9", 123456L,
            "itms-services://?action=download-manifest&url=https://url-to-app/Test_App.plist",
            buildsReleaserEmail
        );
        dataLoadHelper.createBuildIfMissing(
            IOS, "eu.testapp", "ABCDefgh2", "0.21.10", 234567L,
            "itms-services://?action=download-manifest&url=https://url-to-app/Test_App.plist",
            buildsReleaserEmail
        );
        dataLoadHelper.publishBuild(IOS, "eu.testapp", "ABCDefgh1");
        dataLoadHelper.publishBuild(IOS, "eu.testapp", "ABCDefgh2");

        dataLoadHelper.createBuildIfMissing(
            ANDROID, "eu.testapp", "1234", "0.4.0", 345678L,
            "https://url-to-app/Test_App_Android_Release_0.4.0_1234.apk",
            buildsReleaserEmail
        );
        dataLoadHelper.createBuildIfMissing(
            ANDROID, "eu.testapp", "2345", "0.4.1", 456789L,
            "https://url-to-app/Test_App/Android/0.4.1/Test_App_Android_Release_0.4.1_2345.apk",
            buildsReleaserEmail
        );
        dataLoadHelper.publishBuild(ANDROID, "eu.testapp", "1234");
        dataLoadHelper.publishBuild(ANDROID, "eu.testapp", "2345");

        dataLoadHelper.createBuildIfMissing(
            IOS, "eu.testapp2", "ABCDefgh3", "1.0", 987654L,
            "itms-services://?action=download-manifest&url=https://url-to-app/Test_App_2.plist",
            buildsReleaserEmail
        );
        dataLoadHelper.createBuildIfMissing(
            IOS, "eu.testapp2", "ABCDefgh4", "test_release_16", 876543L,
            "itms-services://?action=download-manifest&url=https://url-to-app/Test_App.plist",
            buildsReleaserEmail
        );
        dataLoadHelper.publishBuild(IOS, "eu.testapp2", "ABCDefgh3");
        dataLoadHelper.publishBuild(IOS, "eu.testapp2", "ABCDefgh4");

        dataLoadHelper.createBuildIfMissing(
            ANDROID, "eu.testapp2", "3456", "0.0.1", 765432L,
            "https://url-to-app/app-release.apk",
            buildsReleaserEmail
        );
        dataLoadHelper.createBuildIfMissing(
            ANDROID, "eu.testapp2", "4567", "test_release_15", 654321L,
            "https://url-to-app/app-release.apk",
            buildsReleaserEmail
        );
        dataLoadHelper.publishBuild(ANDROID, "eu.testapp2", "3456");
        dataLoadHelper.publishBuild(ANDROID, "eu.testapp2", "4567");

        return projectDemo1Id;
    }

    private int createProjectDemo2(String buildsReleaserEmail) {
        int projectDemo2Id = dataLoadHelper.createProjectIfMissing("Project Demo 2");

        dataLoadHelper.createAppIfMissing(projectDemo2Id, IOS, "eu.testapp", "Test App iOS");
        dataLoadHelper.createAppIfMissing(projectDemo2Id, ANDROID, "eu.testapp", "Test App Android");
        dataLoadHelper.createAppIfMissing(projectDemo2Id, IOS, "eu.testapp2", "Test App 2 iOS");
        dataLoadHelper.createAppIfMissing(projectDemo2Id, ANDROID, "eu.testapp2", "Test App 2 Android");

        dataLoadHelper.createBuildIfMissing(
            IOS, "eu.testapp", "ABCDefgh1", "0.21.9", 123654L,
            "itms-services://?action=download-manifest&url=https://url-to-app/Test_App.plist",
            buildsReleaserEmail
        );
        dataLoadHelper.createBuildIfMissing(
            IOS, "eu.testapp", "ABCDefgh2", "0.21.10", 234765L,
            "itms-services://?action=download-manifest&url=https://url-to-app/Test_App.plist",
            buildsReleaserEmail
        );
        dataLoadHelper.publishBuild(IOS, "eu.testapp", "ABCDefgh1");
        dataLoadHelper.publishBuild(IOS, "eu.testapp", "ABCDefgh2");

        dataLoadHelper.createBuildIfMissing(
            ANDROID, "eu.testapp", "1234", "0.4.0", 345876L,
            "https://url-to-app/Test_App/Android/0.4.0/Test App_Android_Release_0.4.0_1234.apk",
            buildsReleaserEmail
        );
        dataLoadHelper.createBuildIfMissing(
            ANDROID, "eu.testapp", "2345", "0.4.1", 456987L,
            "https://url-to-app/Test App/Android/0.4.1/Test App_Android_Release_0.4.1_2345.apk",
            buildsReleaserEmail
        );
        dataLoadHelper.publishBuild(ANDROID, "eu.testapp", "1234");
        dataLoadHelper.publishBuild(ANDROID, "eu.testapp", "2345");

        dataLoadHelper.createBuildIfMissing(
            IOS, "eu.testapp2", "ABCDefgh3", "1.0", 987456L,
            "itms-services://?action=download-manifest&url=https://url-to-app/Test_App_2/ios/1.0_1/Test_App_2.plist",
            buildsReleaserEmail
        );
        dataLoadHelper.createBuildIfMissing(
            IOS, "eu.testapp2", "ABCDefgh4", "test_release_16", 876345L,
            "itms-services://?action=download-manifest&url=https://url-to-app/Test_App_2/ios/0.21.10_1626/Test_App_2.plist",
            buildsReleaserEmail
        );
        dataLoadHelper.publishBuild(IOS, "eu.testapp2", "ABCDefgh3");
        dataLoadHelper.publishBuild(IOS, "eu.testapp2", "ABCDefgh4");

        dataLoadHelper.createBuildIfMissing(
            ANDROID, "eu.testapp2", "3456", "0.0.1", 765234L,
            "https://s3.eu-central-1.amazonaws.com/url-to-app/Test_App_2/android/0.0.1_3456/app-release.apk",
            buildsReleaserEmail
        );
        dataLoadHelper.createBuildIfMissing(
            ANDROID, "eu.testapp2", "4567", "test_release_15", 654123L,
            "https://url-to-app/Test_App_2/android/test_release_15_1371/app-release.apk",
            buildsReleaserEmail
        );
        dataLoadHelper.publishBuild(ANDROID, "eu.testapp2", "3456");
        dataLoadHelper.publishBuild(ANDROID, "eu.testapp2", "4567");

        return projectDemo2Id;
    }

    private int createProjectWithoutBuilds() {
        int projectWithoutBuildsId = dataLoadHelper.createProjectIfMissing("Project w/o Builds");

        dataLoadHelper.createAppIfMissing(projectWithoutBuildsId, ANDROID, "app.without.builds", "App w/o builds");
        dataLoadHelper.createAppIfMissing(projectWithoutBuildsId, IOS, "app.without.builds", "App w/o builds");

        return projectWithoutBuildsId;
    }

    private int createProjectWithoutApps() {
        return dataLoadHelper.createProjectIfMissing("Project w/o Apps");
    }

}
