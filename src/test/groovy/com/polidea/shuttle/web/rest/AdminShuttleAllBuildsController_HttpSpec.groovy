package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.app.App
import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.permissions.PermissionType

import static com.polidea.shuttle.domain.app.Platform.determinePlatformFromText
import static com.polidea.shuttle.domain.user.permissions.PermissionType.PUBLISHER
import static com.polidea.shuttle.domain.user.permissions.PermissionType.UNPUBLISHED_BUILDS_VIEWER

class AdminShuttleAllBuildsController_HttpSpec extends HttpIntegrationSpecification {

    String userToken
    String viewerToken
    String shuttleViewerToken
    String shuttlePublisherToken
    String publisherToken
    def releaserEmail = 'releaser@shuttle.com'

    void setup() {
        def shuttleProject = createProject('Shuttle')

        createUser('john@user.com')
        userToken = createAccessTokenFor('john@user.com')

        createUser('viewer@user.com')
        viewerToken = createAccessTokenFor('viewer@user.com')
        assignGlobalPermission('viewer@user.com', UNPUBLISHED_BUILDS_VIEWER)

        createUser('publisher@user.com')
        publisherToken = createAccessTokenFor('publisher@user.com')
        assignGlobalPermission('publisher@user.com', PUBLISHER)

        createUser(releaserEmail, 'Build Releaser')

        createUser('shuttleviewer@user.com')
        shuttleViewerToken = createAccessTokenFor('shuttleviewer@user.com')
        assignProjectPermission(shuttleProject.id(), 'shuttleviewer@user.com', UNPUBLISHED_BUILDS_VIEWER)

        createUser('shuttlepublisher@user.com')
        shuttlePublisherToken = createAccessTokenFor('shuttlepublisher@user.com')
        assignProjectPermission(shuttleProject.id(), 'shuttlepublisher@user.com', PUBLISHER)

        def appIdIos = 'com.polidea.shuttle'
        def platformIos = 'ios'
        createApp(shuttleProject, platformIos, appIdIos, 'Any App')

        def appIdAndroid = 'com.polidea.shuttle'
        def platformAndroid = 'android'
        createApp(shuttleProject, platformAndroid, appIdAndroid, 'Any App')

        createBuild(appIdIos, platformIos, 'ios.prefix.schema.123')
        createBuild(appIdIos, platformAndroid, '123')

        createBuild(appIdIos, platformIos, 'ios.prefix.schema.124', '1.2.4')
        createBuild(appIdIos, platformAndroid, '124', '1.2.4')
    }

    def "users who can see only published builds should not see latest not published builds"() {
        when:
        def userResponse = get("/admin/shuttle/builds", userToken)

        then:
        userResponse.code() == 200

        userResponse.body().allBuilds == null
    }

    def "global viewer should see latest not published builds"() {
        when:
        def userResponse = get("/admin/shuttle/builds", viewerToken)

        then:
        userResponse.code() == 200

        userResponse.body().allBuilds.android.href != null
        userResponse.body().allBuilds.android.qrCodeBase64 != null
        userResponse.body().allBuilds.android.version == '1.2.4'

        userResponse.body().allBuilds.ios.href != null
        userResponse.body().allBuilds.ios.qrCodeBase64 != null
        userResponse.body().allBuilds.ios.version == '1.2.4'
    }

    def "shuttle viewer should see latest not published builds"() {
        when:
        def userResponse = get("/admin/shuttle/builds", shuttleViewerToken)

        then:
        userResponse.code() == 200

        userResponse.body().allBuilds.android.href != null
        userResponse.body().allBuilds.android.qrCodeBase64 != null
        userResponse.body().allBuilds.android.version == '1.2.4'

        userResponse.body().allBuilds.ios.href != null
        userResponse.body().allBuilds.ios.qrCodeBase64 != null
        userResponse.body().allBuilds.ios.version == '1.2.4'
    }

    def "global publisher should see latest not published builds"() {
        when:
        def userResponse = get("/admin/shuttle/builds", publisherToken)

        then:
        userResponse.code() == 200

        userResponse.body().allBuilds.android.href != null
        userResponse.body().allBuilds.android.qrCodeBase64 != null
        userResponse.body().allBuilds.android.version == '1.2.4'

        userResponse.body().allBuilds.ios.href != null
        userResponse.body().allBuilds.ios.qrCodeBase64 != null
        userResponse.body().allBuilds.ios.version == '1.2.4'
    }

    def "shuttle publisher should see latest not published builds"() {
        when:
        def userResponse = get("/admin/shuttle/builds", shuttlePublisherToken)

        then:
        userResponse.code() == 200

        userResponse.body().allBuilds.android.href != null
        userResponse.body().allBuilds.android.qrCodeBase64 != null
        userResponse.body().allBuilds.android.version == '1.2.4'

        userResponse.body().allBuilds.ios.href != null
        userResponse.body().allBuilds.ios.qrCodeBase64 != null
        userResponse.body().allBuilds.ios.version == '1.2.4'
    }

    def "publisher should see all (even not published) latest builds"() {
        when:
        publishBuild('com.polidea.shuttle', 'ios', 'ios.prefix.schema.123')
        publishBuild('com.polidea.shuttle', 'android', '123')
        def userResponse = get("/admin/shuttle/builds", publisherToken)

        then:
        userResponse.code() == 200

        userResponse.body().allBuilds.android.href != null
        userResponse.body().allBuilds.android.qrCodeBase64 != null
        userResponse.body().allBuilds.android.version == '1.2.4'

        userResponse.body().allBuilds.ios.href != null
        userResponse.body().allBuilds.ios.qrCodeBase64 != null
        userResponse.body().allBuilds.ios.version == '1.2.4'

        userResponse.body().publishedBuilds.android.href != null
        userResponse.body().publishedBuilds.android.qrCodeBase64 != null
        userResponse.body().publishedBuilds.android.version == '1.2.3'

        userResponse.body().publishedBuilds.ios.href != null
        userResponse.body().publishedBuilds.ios.qrCodeBase64 != null
        userResponse.body().publishedBuilds.ios.version == '1.2.3'
    }

    def "user who can see not published builds should see latest builds when all builds are published"() {
        when:
        publishBuild('com.polidea.shuttle', 'ios', 'ios.prefix.schema.123')
        publishBuild('com.polidea.shuttle', 'android', '123')
        publishBuild('com.polidea.shuttle', 'ios', 'ios.prefix.schema.124')
        publishBuild('com.polidea.shuttle', 'android', '124')
        def userResponse = get("/admin/shuttle/builds", publisherToken)

        then:
        userResponse.code() == 200

        userResponse.body().allBuilds.android.href != null
        userResponse.body().allBuilds.android.qrCodeBase64 != null
        userResponse.body().allBuilds.android.version == '1.2.4'

        userResponse.body().allBuilds.ios.href != null
        userResponse.body().allBuilds.ios.qrCodeBase64 != null
        userResponse.body().allBuilds.ios.version == '1.2.4'

        userResponse.body().publishedBuilds.android.href != null
        userResponse.body().publishedBuilds.android.qrCodeBase64 != null
        userResponse.body().publishedBuilds.android.version == '1.2.4'

        userResponse.body().publishedBuilds.ios.href != null
        userResponse.body().publishedBuilds.ios.qrCodeBase64 != null
        userResponse.body().publishedBuilds.ios.version == '1.2.4'
    }

    private void createUser(String userEmail) {
        setupHelper.createUser(userEmail, "Name of ${userEmail}", null)
    }

    private String createAccessTokenFor(String userEmail) {
        return setupHelper.createClientAccessToken(userEmail, 'any-device-id')
    }

    private User createUser(String userEmail, String name) {
        setupHelper.createUser(userEmail, name, null)
    }

    private assignGlobalPermission(String userEmail, PermissionType permission) {
        setupHelper.assignGlobalPermissions(userEmail, [permission])
    }

    private assignProjectPermission(Integer projectId, String userEmail, PermissionType permission) {
        setupHelper.assignProjectPermission(projectId, userEmail, permission)
    }

    private Project createProject(String name) {
        setupHelper.createProject(name, null)
    }

    private App createApp(Project project, String platform, String appId, String name) {
        setupHelper.createApp(project, determinePlatformFromText(platform), appId, name, null)
    }

    private publishBuild(String appId, String platform, String buildIdentifier) {
        setupHelper.publishBuild(determinePlatformFromText(platform), appId, buildIdentifier);
    }

    private createBuild(String appId, String platform, String buildIdentifier) {
        setupHelper.createBuild(
                appId,
                determinePlatformFromText(platform),
                releaserEmail,
                buildIdentifier,
                '1.2.3',
                'Any Release Notes',
                'http://href.to.app',
                123456789
        )
    }

    private createBuild(String appId, String platform, String buildIdentifier, String buildVersion) {
        setupHelper.createBuild(
                appId,
                determinePlatformFromText(platform),
                releaserEmail,
                buildIdentifier,
                buildVersion,
                'Any Release Notes',
                'http://href.to.app',
                123456789
        )
    }
}
