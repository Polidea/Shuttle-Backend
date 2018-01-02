package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.app.App
import com.polidea.shuttle.domain.notifications.NotificationsSenderService
import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.permissions.PermissionType
import org.springframework.beans.factory.annotation.Autowired

import static com.polidea.shuttle.domain.app.Platform.determinePlatformFromText

class ClientProjectController_LatestBuilds_HttpSpec extends HttpIntegrationSpecification {

    public static final String ANY_PUBLISHER_EMAIL = 'any.publisher@shuttle.com'
    public static final String NOT_PUBLISHER_EMAIL = 'not.publisher@shuttle.com'
    public static final String ANY_VIEWER_EMAIL = 'any.viewer@shuttle.com'

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

        def viewer = createUser('any.viewer@shuttle.com', 'Viewer')
        assignGlobalPermission(viewer.email(), PermissionType.UNPUBLISHED_BUILDS_VIEWER)
        viewerToken = createClientAccessTokenFor(viewer.email())

        def notPublisher = createUser(NOT_PUBLISHER_EMAIL, 'Not Publisher')
        notPublisherToken = createClientAccessTokenFor(notPublisher.email())

        releaserEmail = 'releaser@shuttle.com'
        createUser(releaserEmail, 'Build Releaser')
    }

    def "List of projects should contain latest builds for iOS app for given project for publisher"() {
        given:
        project = createProject('Any Project')

        assignUserToProject(ANY_PUBLISHER_EMAIL, project)

        appId = 'any.app.id'
        platform = 'ios'
        createApp(project, platform, appId, 'Any App')

        buildIdentifier = 'ios.prefix.schema.123'
        createBuild(appId, platform, buildIdentifier)

        when:
        def buildsResponse = get('/projects', publisherToken)

        then:
        buildsResponse.body().projects.size() == 1
        buildsResponse.body().projects[0].latestAndroidBuilds.size() == 0
        buildsResponse.body().projects[0].latestIosBuilds.size() == 1
        buildsResponse.body().projects[0].latestIosBuilds[0].appId == 'any.app.id'
        buildsResponse.body().projects[0].latestIosBuilds[0].lastReleaseDate != null
        buildsResponse.body().projects[0].latestIosBuilds[0].prefixSchema == 'ios.prefix.schema.123'
    }

    def "List of projects should contain latest builds for iOS app for given project for viewer of not published builds"() {
        given:
        project = createProject('Any Project')

        assignUserToProject(ANY_VIEWER_EMAIL, project)

        appId = 'any.app.id'
        platform = 'ios'
        createApp(project, platform, appId, 'Any App')

        buildIdentifier = 'ios.prefix.schema.123'
        createBuild(appId, platform, buildIdentifier)

        when:
        def buildsResponse = get('/projects', viewerToken)

        then:
        buildsResponse.body().projects.size() == 1
        buildsResponse.body().projects[0].latestAndroidBuilds.size() == 0
        buildsResponse.body().projects[0].latestIosBuilds.size() == 1
        buildsResponse.body().projects[0].latestIosBuilds[0].appId == 'any.app.id'
        buildsResponse.body().projects[0].latestIosBuilds[0].lastReleaseDate != null
        buildsResponse.body().projects[0].latestIosBuilds[0].prefixSchema == 'ios.prefix.schema.123'
    }

    def "List of projects should contain latest builds for Android app for given project for publisher"() {
        given:
        project = createProject('Any Project')

        assignUserToProject(ANY_PUBLISHER_EMAIL, project)

        appId = 'any.app.id'
        platform = 'android'
        createApp(project, platform, appId, 'Any App')

        buildIdentifier = '123'
        createBuild(appId, platform, buildIdentifier)

        when:
        def buildsResponse = get('/projects', publisherToken)

        then:
        buildsResponse.body().projects.size() == 1
        buildsResponse.body().projects[0].latestIosBuilds.size() == 0
        buildsResponse.body().projects[0].latestAndroidBuilds.size() == 1
        buildsResponse.body().projects[0].latestAndroidBuilds[0].appId == 'any.app.id'
        buildsResponse.body().projects[0].latestAndroidBuilds[0].lastReleaseDate != null
        buildsResponse.body().projects[0].latestAndroidBuilds[0].versionCode == 123
    }

    def "List of projects should contain latest builds for Android app for given project for viewer of not published builds"() {
        given:
        project = createProject('Any Project')

        assignUserToProject(ANY_VIEWER_EMAIL, project)

        appId = 'any.app.id'
        platform = 'android'
        createApp(project, platform, appId, 'Any App')

        buildIdentifier = '123'
        createBuild(appId, platform, buildIdentifier)

        when:
        def buildsResponse = get('/projects', viewerToken)

        then:
        buildsResponse.body().projects.size() == 1
        buildsResponse.body().projects[0].latestIosBuilds.size() == 0
        buildsResponse.body().projects[0].latestAndroidBuilds.size() == 1
        buildsResponse.body().projects[0].latestAndroidBuilds[0].appId == 'any.app.id'
        buildsResponse.body().projects[0].latestAndroidBuilds[0].lastReleaseDate != null
        buildsResponse.body().projects[0].latestAndroidBuilds[0].versionCode == 123
    }

    def "List of projects should contain latest builds out of three for iOS app for given project for publisher"() {
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
        def buildsResponse = get('/projects', publisherToken)

        then:
        buildsResponse.body().projects.size() == 1
        buildsResponse.body().projects[0].latestIosBuilds.size() == 1
        buildsResponse.body().projects[0].latestIosBuilds[0].prefixSchema == 'ios.prefix.schema.125'
    }

    def "List of projects should contain latest builds out of three for Android app for given project for publisher"() {
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
        def buildsResponse = get('/projects', publisherToken)

        then:
        buildsResponse.body().projects.size() == 1
        buildsResponse.body().projects[0].latestAndroidBuilds.size() == 1
        buildsResponse.body().projects[0].latestAndroidBuilds[0].versionCode == 125
    }

    def "List of projects should contain 0 latest builds for not published iOS app for given project for not publisher"() {
        given:
        project = createProject('Any Project')

        assignUserToProject(NOT_PUBLISHER_EMAIL, project)

        appId = 'any.app.id'
        platform = 'ios'
        createApp(project, platform, appId, 'Any App')

        buildIdentifier = 'ios.prefix.schema.123'
        createBuild(appId, platform, buildIdentifier)

        when:
        def buildsResponse = get('/projects', notPublisherToken)

        then:
        buildsResponse.body().projects.size() == 1
        buildsResponse.body().projects[0].latestIosBuilds.size() == 0
        buildsResponse.body().projects[0].latestAndroidBuilds.size() == 0
        buildsResponse.body().projects[0].lastReleaseDate == null
    }

    def "List of projects should contain 0 latest builds for not published Android app for given project for not publisher"() {
        given:
        project = createProject('Any Project')

        assignUserToProject(NOT_PUBLISHER_EMAIL, project)

        appId = 'any.app.id'
        platform = 'android'
        createApp(project, platform, appId, 'Any App')

        buildIdentifier = '123'
        createBuild(appId, platform, buildIdentifier)

        when:
        def buildsResponse = get('/projects', notPublisherToken)

        then:
        buildsResponse.body().projects.size() == 1
        buildsResponse.body().projects[0].latestIosBuilds.size() == 0
        buildsResponse.body().projects[0].latestAndroidBuilds.size() == 0
        buildsResponse.body().projects[0].lastReleaseDate == null
    }

    def "List of projects should contain one latest published build out of three for iOS app for given project for not publisher"() {
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
        def buildsResponse = get('/projects', notPublisherToken)

        then:
        buildsResponse.body().projects.size() == 1
        buildsResponse.body().projects[0].latestIosBuilds.size() == 1
        buildsResponse.body().projects[0].latestIosBuilds[0].prefixSchema == 'ios.prefix.schema.124'
    }

    def "List of projects should contain one latest published build out of three for Android app for given project for not publisher"() {
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
        def buildsResponse = get('/projects', notPublisherToken)

        then:
        buildsResponse.body().projects.size() == 1
        buildsResponse.body().projects[0].latestAndroidBuilds.size() == 1
        buildsResponse.body().projects[0].latestAndroidBuilds[0].versionCode == 124
    }

    def "List of projects should contain two latest published builds for two Android apps for given project for not publisher"() {
        given:
        project = createProject('Any Project')

        assignUserToProject(NOT_PUBLISHER_EMAIL, project)

        def appId1 = 'any.app.id1'
        def appId2 = 'any.app.id2'

        platform = 'android'
        createApp(project, platform, appId1, 'Any App 1')
        createApp(project, platform, appId2, 'Any App 2')

        def buildIdentifier1 = '123'
        createBuild(appId1, platform, buildIdentifier1)

        def buildIdentifier2 = '124'
        createBuild(appId1, platform, buildIdentifier2)

        def buildIdentifier3 = '125'
        createBuild(appId1, platform, buildIdentifier3)

        def buildIdentifier4 = '126'
        createBuild(appId2, platform, buildIdentifier4)

        def buildIdentifier5 = '127'
        createBuild(appId2, platform, buildIdentifier5)

        def buildIdentifier6 = '128'
        createBuild(appId2, platform, buildIdentifier6)

        publishBuild(project, platform, appId1, buildIdentifier2)
        publishBuild(project, platform, appId2, buildIdentifier5)

        when:
        def buildsResponse = get('/projects', notPublisherToken)

        then:
        buildsResponse.body().projects.size() == 1
        buildsResponse.body().projects[0].latestAndroidBuilds.size() == 2
        buildsResponse.body().projects[0].latestAndroidBuilds.find { it.appId == 'any.app.id1' }.versionCode == 124
        buildsResponse.body().projects[0].latestAndroidBuilds.find { it.appId == 'any.app.id2' }.versionCode == 127
    }

    def "List of projects should contain two latest published builds for two iOS apps for given project for not publisher"() {
        given:
        project = createProject('Any Project')

        assignUserToProject(NOT_PUBLISHER_EMAIL, project)

        def appId1 = 'any.app.id1'
        def appId2 = 'any.app.id2'

        platform = 'ios'
        createApp(project, platform, appId1, 'Any App 1')
        createApp(project, platform, appId2, 'Any App 2')

        def buildIdentifier1 = 'ios.prefix.schema.123'
        createBuild(appId1, platform, buildIdentifier1)

        def buildIdentifier2 = 'ios.prefix.schema.124'
        createBuild(appId1, platform, buildIdentifier2)

        def buildIdentifier3 = 'ios.prefix.schema.125'
        createBuild(appId1, platform, buildIdentifier3)

        def buildIdentifier4 = 'ios.prefix.schema.126'
        createBuild(appId2, platform, buildIdentifier4)

        def buildIdentifier5 = 'ios.prefix.schema.127'
        createBuild(appId2, platform, buildIdentifier5)

        def buildIdentifier6 = 'ios.prefix.schema.128'
        createBuild(appId2, platform, buildIdentifier6)

        publishBuild(project, platform, appId1, buildIdentifier2)
        publishBuild(project, platform, appId2, buildIdentifier5)

        when:
        def buildsResponse = get('/projects', notPublisherToken)

        then:
        buildsResponse.body().projects.size() == 1
        buildsResponse.body().projects[0].latestIosBuilds.size() == 2
        buildsResponse.body().projects[0].latestIosBuilds.find { it.appId == 'any.app.id1' }.prefixSchema == 'ios.prefix.schema.124'
        buildsResponse.body().projects[0].latestIosBuilds.find { it.appId == 'any.app.id2' }.prefixSchema == 'ios.prefix.schema.127'
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
