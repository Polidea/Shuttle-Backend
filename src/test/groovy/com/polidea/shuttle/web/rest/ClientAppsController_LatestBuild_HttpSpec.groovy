package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.app.App
import com.polidea.shuttle.domain.notifications.NotificationsSenderService
import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.permissions.PermissionType
import org.springframework.beans.factory.annotation.Autowired

import static com.polidea.shuttle.domain.app.Platform.determinePlatformFromText

class ClientAppsController_LatestBuild_HttpSpec extends HttpIntegrationSpecification {

    public static final String ANY_PUBLISHER_EMAIL = 'any.publisher@shuttle.com'
    public static final String ANY_VIEWER_EMAIL = 'any.viewer@shuttle.com'
    public static final String NOT_PUBLISHER_EMAIL = 'not.publisher@shuttle.com'

    @Autowired
    NotificationsSenderService notificationsSenderService

    String publisherToken
    String notPublisherToken
    String viewerToken
    Project project
    String appId
    String platform
    String buildIdentifier
    String releaserEmail

    def setup() {
        def publisher = createUser(ANY_PUBLISHER_EMAIL, 'Publisher')
        assignGlobalPermission(publisher.email(), PermissionType.PUBLISHER)
        publisherToken = createClientAccessTokenFor(publisher.email())

        def viewer = createUser(ANY_VIEWER_EMAIL, 'Publisher')
        assignGlobalPermission(viewer.email(), PermissionType.UNPUBLISHED_BUILDS_VIEWER)
        viewerToken = createClientAccessTokenFor(viewer.email())

        def notPublisher = createUser(NOT_PUBLISHER_EMAIL, 'Not Publisher')
        notPublisherToken = createClientAccessTokenFor(notPublisher.email())

        releaserEmail = 'releaser@shuttle.com'
        createUser(releaserEmail, 'Build Releaser')
    }

    def "App in list of apps should contain latest not published build for iOS app for given project for publisher"() {
        given:
        project = createProject('Any Project')

        assignUserToProject(ANY_PUBLISHER_EMAIL, project)

        appId = 'any.app.id'
        platform = 'ios'
        createApp(project, platform, appId, 'Any App')

        buildIdentifier = 'ios.prefix.schema.123'
        createBuild(appId, platform, buildIdentifier)

        when:
        def appsResponse = get("/projects/${project.id}/apps/${platform}", publisherToken)

        then:
        appsResponse.body().apps.size() == 1
        appsResponse.body().apps[0].latestBuild.version == '1.2.3'
        appsResponse.body().apps[0].latestBuild.prefixSchema == 'ios.prefix.schema.123'
        appsResponse.body().apps[0].latestBuild.href == 'http://href.to.app'
        appsResponse.body().apps[0].latestBuild.releaseDate != null
        appsResponse.body().apps[0].latestBuild.bytes == 123456789
    }

    def "App in list of apps should contain latest not published build for Android app for given project for publisher"() {
        given:
        project = createProject('Any Project')

        assignUserToProject(ANY_PUBLISHER_EMAIL, project)

        appId = 'any.app.id'
        platform = 'android'
        createApp(project, platform, appId, 'Any App')

        buildIdentifier = '123'
        createBuild(appId, platform, buildIdentifier)

        when:
        def appsResponse = get("/projects/${project.id}/apps/${platform}", publisherToken)

        then:
        appsResponse.body().apps.size() == 1
        appsResponse.body().apps[0].latestBuild.version == '1.2.3'
        appsResponse.body().apps[0].latestBuild.versionCode == 123
        appsResponse.body().apps[0].latestBuild.href == 'http://href.to.app'
        appsResponse.body().apps[0].latestBuild.releaseDate != null
        appsResponse.body().apps[0].latestBuild.bytes == 123456789
    }

    def "App in list of apps should contain latest not published build out of three for iOS app for given project for publisher"() {
        given:
        project = createProject('Any Project')

        assignUserToProject(ANY_PUBLISHER_EMAIL, project)

        appId = 'any.app.id'
        platform = 'ios'
        createApp(project, platform, appId, 'Any App')

        def buildIdentifier1 = 'ios.prefix.schema.123'
        createBuild(appId, platform, buildIdentifier1)

        def buildIdentifier2 = 'ios.prefix.schema.124'
        createBuild(appId, platform, buildIdentifier2)

        def buildIdentifier3 = 'ios.prefix.schema.125'
        createBuild(appId, platform, buildIdentifier3)

        when:
        def appsResponse = get("/projects/${project.id}/apps/${platform}", publisherToken)

        then:
        appsResponse.body().apps.size() == 1
        appsResponse.body().apps[0].latestBuild.version == '1.2.3'
        appsResponse.body().apps[0].latestBuild.prefixSchema == 'ios.prefix.schema.125'
        appsResponse.body().apps[0].latestBuild.href == 'http://href.to.app'
        appsResponse.body().apps[0].latestBuild.releaseDate != null
        appsResponse.body().apps[0].latestBuild.bytes == 123456789
    }

    def "App in list of apps should contain latest not published build out of three for Android app for given project for publisher"() {
        given:
        project = createProject('Any Project')

        assignUserToProject(ANY_PUBLISHER_EMAIL, project)

        appId = 'any.app.id'
        platform = 'android'
        createApp(project, platform, appId, 'Any App')

        def buildIdentifier1 = '123'
        createBuild(appId, platform, buildIdentifier1)

        def buildIdentifier2 = '124'
        createBuild(appId, platform, buildIdentifier2)

        def buildIdentifier3 = '125'
        createBuild(appId, platform, buildIdentifier3)

        when:
        def appsResponse = get("/projects/${project.id}/apps/${platform}", publisherToken)

        then:
        appsResponse.body().apps.size() == 1
        appsResponse.body().apps[0].latestBuild.version == '1.2.3'
        appsResponse.body().apps[0].latestBuild.versionCode == 125
        appsResponse.body().apps[0].latestBuild.href == 'http://href.to.app'
        appsResponse.body().apps[0].latestBuild.releaseDate != null
        appsResponse.body().apps[0].latestBuild.bytes == 123456789
        appsResponse.body().apps[0].lastReleaseDate != null
    }

    def "App in list of apps should contain latest not published build out of three for Android app for given project for viewer of not pulished builds"() {
        given:
        project = createProject('Any Project')

        assignUserToProject(ANY_VIEWER_EMAIL, project)

        appId = 'any.app.id'
        platform = 'android'
        createApp(project, platform, appId, 'Any App')

        def buildIdentifier1 = '123'
        createBuild(appId, platform, buildIdentifier1)

        def buildIdentifier2 = '124'
        createBuild(appId, platform, buildIdentifier2)

        def buildIdentifier3 = '125'
        createBuild(appId, platform, buildIdentifier3)

        when:
        def appsResponse = get("/projects/${project.id}/apps/${platform}", viewerToken)

        then:
        appsResponse.body().apps.size() == 1
        appsResponse.body().apps[0].latestBuild.version == '1.2.3'
        appsResponse.body().apps[0].latestBuild.versionCode == 125
        appsResponse.body().apps[0].latestBuild.href == 'http://href.to.app'
        appsResponse.body().apps[0].latestBuild.releaseDate != null
        appsResponse.body().apps[0].latestBuild.bytes == 123456789
        appsResponse.body().apps[0].lastReleaseDate != null
    }

    def "App in list of apps should not contain latest build for not published build out of three for iOS app for given project for not publisher"() {
        given:
        project = createProject('Any Project')

        assignUserToProject(NOT_PUBLISHER_EMAIL, project)

        appId = 'any.app.id'
        platform = 'ios'
        createApp(project, platform, appId, 'Any App')

        def buildIdentifier1 = 'ios.prefix.schema.123'
        createBuild(appId, platform, buildIdentifier1)

        def buildIdentifier2 = 'ios.prefix.schema.124'
        createBuild(appId, platform, buildIdentifier2)

        def buildIdentifier3 = 'ios.prefix.schema.125'
        createBuild(appId, platform, buildIdentifier3)

        when:
        def appsResponse = get("/projects/${project.id}/apps/${platform}", notPublisherToken)

        then:
        appsResponse.body().apps.size() == 1
        appsResponse.body().apps[0].latestBuild == null
        appsResponse.body().apps[0].lastReleaseDate == null
    }

    def "App in list of apps should not contain latest build for not published build out of three for Android app for given project for not publisher"() {
        given:
        project = createProject('Any Project')

        assignUserToProject(NOT_PUBLISHER_EMAIL, project)

        appId = 'any.app.id'
        platform = 'android'
        createApp(project, platform, appId, 'Any App')

        def buildIdentifier1 = '123'
        createBuild(appId, platform, buildIdentifier1)

        def buildIdentifier2 = '124'
        createBuild(appId, platform, buildIdentifier2)

        def buildIdentifier3 = '125'
        createBuild(appId, platform, buildIdentifier3)

        when:
        def appsResponse = get("/projects/${project.id}/apps/${platform}", notPublisherToken)

        then:
        appsResponse.body().apps.size() == 1
        appsResponse.body().apps[0].latestBuild == null
    }

    def "App in list of apps should contain latest published build out of three for iOS app for given project for not publisher"() {
        given:
        project = createProject('Any Project')

        assignUserToProject(NOT_PUBLISHER_EMAIL, project)

        appId = 'any.app.id'
        platform = 'ios'
        createApp(project, platform, appId, 'Any App')

        def buildIdentifier1 = 'ios.prefix.schema.123'
        createBuild(appId, platform, buildIdentifier1)

        def buildIdentifier2 = 'ios.prefix.schema.124'
        createBuild(appId, platform, buildIdentifier2)

        def buildIdentifier3 = 'ios.prefix.schema.125'
        createBuild(appId, platform, buildIdentifier3)

        publishBuild(project, platform, appId, buildIdentifier2)

        when:
        def appsResponse = get("/projects/${project.id}/apps/${platform}", notPublisherToken)

        then:
        appsResponse.body().apps.size() == 1
        appsResponse.body().apps[0].latestBuild.prefixSchema == 'ios.prefix.schema.124'
    }

    def "App in list of apps should contain latest published build out of three for Android app for given project for not publisher"() {
        given:
        project = createProject('Any Project')

        assignUserToProject(NOT_PUBLISHER_EMAIL, project)

        appId = 'any.app.id'
        platform = 'android'
        createApp(project, platform, appId, 'Any App')

        def buildIdentifier1 = '123'
        createBuild(appId, platform, buildIdentifier1)

        def buildIdentifier2 = '124'
        createBuild(appId, platform, buildIdentifier2)

        def buildIdentifier3 = '125'
        createBuild(appId, platform, buildIdentifier3)

        publishBuild(project, platform, appId, buildIdentifier2)

        when:
        def appsResponse = get("/projects/${project.id}/apps/${platform}", notPublisherToken)

        then:
        appsResponse.body().apps.size() == 1
        appsResponse.body().apps[0].latestBuild.versionCode == 124
    }

    def "App in list of apps should contain latest released build out of three for iOS app for given project for publisher even if one is published"() {
        given:
        project = createProject('Any Project')

        assignUserToProject(ANY_PUBLISHER_EMAIL, project)

        appId = 'any.app.id'
        platform = 'ios'
        createApp(project, platform, appId, 'Any App')

        def buildIdentifier1 = 'ios.prefix.schema.123'
        createBuild(appId, platform, buildIdentifier1)

        def buildIdentifier2 = 'ios.prefix.schema.124'
        createBuild(appId, platform, buildIdentifier2)

        def buildIdentifier3 = 'ios.prefix.schema.125'
        createBuild(appId, platform, buildIdentifier3)

        publishBuild(project, platform, appId, buildIdentifier2)

        when:
        def appsResponse = get("/projects/${project.id}/apps/${platform}", publisherToken)

        then:
        appsResponse.body().apps.size() == 1
        appsResponse.body().apps[0].latestBuild.prefixSchema == 'ios.prefix.schema.125'
    }

    def "App in list of apps should contain latest released build out of three for Android app for given project for publisher even if one is published"() {
        given:
        project = createProject('Any Project')

        assignUserToProject(ANY_PUBLISHER_EMAIL, project)

        appId = 'any.app.id'
        platform = 'android'
        createApp(project, platform, appId, 'Any App')

        def buildIdentifier1 = '123'
        createBuild(appId, platform, buildIdentifier1)

        def buildIdentifier2 = '124'
        createBuild(appId, platform, buildIdentifier2)

        def buildIdentifier3 = '125'
        createBuild(appId, platform, buildIdentifier3)

        publishBuild(project, platform, appId, buildIdentifier2)

        when:
        def appsResponse = get("/projects/${project.id}/apps/${platform}", publisherToken)

        then:
        appsResponse.body().apps.size() == 1
        appsResponse.body().apps[0].latestBuild.versionCode == 125
    }

    private User createUser(String userEmail, String name) {
        setupHelper.createUser(userEmail, name, null)
    }

    private assignGlobalPermission(String userEmail, PermissionType permission) {
        setupHelper.assignGlobalPermissions(userEmail, [permission])
    }

    private String createClientAccessTokenFor(String userEmail) {
        setupHelper.createClientAccessToken(userEmail, 'any-device-id')
    }

    private Project createProject(String name) {
        setupHelper.createProject(name, null)
    }

    private assignUserToProject(String userEmail, Project project) {
        setupHelper.assignUserToProject(userEmail, project)
    }

    private App createApp(Project project, String platform, String appId, String name) {
        setupHelper.createApp(project, determinePlatformFromText(platform), appId, name, null)
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

    private publishBuild(Project project, String platform, String appId, String buildIdentifier) {
        post("/projects/${project.id}/apps/${platform}/${appId}/builds/${buildIdentifier}/publish", publisherToken)
    }
}
