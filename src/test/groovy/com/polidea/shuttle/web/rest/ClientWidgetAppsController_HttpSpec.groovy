package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.app.App
import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.permissions.PermissionType

import static com.polidea.shuttle.domain.app.Platform.determinePlatformFromText

class ClientWidgetAppsController_HttpSpec extends HttpIntegrationSpecification {

    String userEmailPublisher
    String userEmailNotPublisher
    String userEmailViewer
    String publisherToken
    String notPublisherToken
    String viewerToken
    Project sadAppProject
    Project happyAppProject
    String sadAppId
    String happyAppId
    String platform
    String sadAppBuildNotPublishedIdentifier1
    String sadAppBuildNotPublishedIdentifier2
    String happyAppBuildNotPublishedIdentifier1
    String happyAppBuildNotPublishedIdentifier2

    def setup() {
        userEmailPublisher = 'any.publisher@shuttle.com'
        createUser(userEmailPublisher, 'Publisher')
        assignGlobalPermission(userEmailPublisher, PermissionType.PUBLISHER)
        publisherToken = createClientAccessTokenFor(userEmailPublisher)

        userEmailViewer = 'any.viewer@shuttle.com'
        def viewer = createUser(userEmailViewer, 'Viewer')
        assignGlobalPermission(viewer.email(), PermissionType.UNPUBLISHED_BUILDS_VIEWER)
        viewerToken = createClientAccessTokenFor(viewer.email())

        userEmailNotPublisher = 'any.not.publisher@shuttle.com'
        createUser(userEmailNotPublisher, 'Not Publisher')
        notPublisherToken = createClientAccessTokenFor(userEmailNotPublisher)

        platform = 'ios'
    }

    def "Listing all apps by released date for global publisher"() {
        given:
        createSadProjectAndItsAppsAndBuilds()
        createHappyProjectAndItsAppsAndBuilds()
        assignUserToProject(userEmailPublisher, sadAppProject)
        assignUserToProject(userEmailPublisher, happyAppProject)

        when:
        def appsResponse = get("/apps/${platform}/by-release-date", publisherToken)
        assert appsResponse.code() == 200

        then:
        appsResponse.body().appsByLastReleaseDate.size() == 2
    }

    def "Listing all apps by released date for global viewer"() {
        given:
        createSadProjectAndItsAppsAndBuilds()
        createHappyProjectAndItsAppsAndBuilds()
        assignUserToProject(userEmailViewer, sadAppProject)
        assignUserToProject(userEmailViewer, happyAppProject)

        when:
        def appsResponse = get("/apps/${platform}/by-release-date", viewerToken)
        assert appsResponse.code() == 200

        then:
        appsResponse.body().appsByLastReleaseDate.size() == 2
    }

    def "Listing no apps by released date for not publisher"() {
        given:
        createSadProjectAndItsAppsAndBuilds()
        createHappyProjectAndItsAppsAndBuilds()
        assignUserToProject(userEmailNotPublisher, sadAppProject)
        assignUserToProject(userEmailNotPublisher, happyAppProject)

        when:
        def appsResponse = get("/apps/${platform}/by-release-date", notPublisherToken)
        assert appsResponse.code() == 200

        then:
        appsResponse.body().appsByLastReleaseDate.size() == 0
    }

    def "Listing one app by released date for not publisher by publishing one app"() {
        given:
        createSadProjectAndItsAppsAndBuilds()
        createHappyProjectAndItsAppsAndBuilds()
        assignUserToProject(userEmailNotPublisher, sadAppProject)
        assignUserToProject(userEmailNotPublisher, happyAppProject)

        when:
        publishSadAppBuild()
        def appsResponse = get("/apps/${platform}/by-release-date", notPublisherToken)
        assert appsResponse.code() == 200

        then:
        appsResponse.body().appsByLastReleaseDate.size() == 1
        appsResponse.body().appsByLastReleaseDate[0].projectId == sadAppProject.id
        appsResponse.body().appsByLastReleaseDate[0].app.id == sadAppId
    }

    def "Listing two apps by released date for not publisher by publishing one app"() {
        given:
        createSadProjectAndItsAppsAndBuilds()
        createHappyProjectAndItsAppsAndBuilds()
        assignUserToProject(userEmailPublisher, sadAppProject)
        assignUserToProject(userEmailPublisher, happyAppProject)
        assignUserToProject(userEmailNotPublisher, sadAppProject)
        assignUserToProject(userEmailNotPublisher, happyAppProject)

        when:
        post("/projects/${sadAppProject.id}/apps/${platform}/${sadAppId}/builds/${sadAppBuildNotPublishedIdentifier1}/publish", publisherToken)
        post("/projects/${happyAppProject.id}/apps/${platform}/${happyAppId}/builds/${happyAppBuildNotPublishedIdentifier1}/publish", publisherToken)
        def appsResponse = get("/apps/${platform}/by-release-date", notPublisherToken)
        assert appsResponse.code() == 200

        then:
        appsResponse.body().appsByLastReleaseDate.size() == 2
        appsResponse.body().appsByLastReleaseDate[0].projectId == happyAppProject.id
        appsResponse.body().appsByLastReleaseDate[0].app.id == happyAppId
        appsResponse.body().appsByLastReleaseDate[1].projectId == sadAppProject.id
        appsResponse.body().appsByLastReleaseDate[1].app.id == sadAppId
    }

    def "App should use proper icon in the response"(String projectIcon, String appIcon, String exposedIcon) {
        given:
        def project = createProject('App Project', projectIcon)
        assignUserToProject(userEmailPublisher, project)
        createApp(project, platform, 'any.app.id', 'Any App', appIcon)
        createBuild('any.app.id', platform, 'any.build.identifier')

        when:
        def appsResponse = get("/apps/${platform}/by-release-date", publisherToken)
        assert appsResponse.code() == 200

        then:
        appsResponse.body().appsByLastReleaseDate[0].app.iconHref == exposedIcon

        where:
        projectIcon           | appIcon           | exposedIcon
        'http://project.icon' | 'http://app.icon' | 'http://app.icon'
        null                  | 'http://app.icon' | 'http://app.icon'
        'http://project.icon' | null              | 'http://project.icon'
        null                  | null              | null
    }

    private createHappyProjectAndItsAppsAndBuilds() {
        happyAppProject = createProject('Happy App Project')
        happyAppId = 'happy.app.id'

        createApp(happyAppProject, platform, happyAppId, 'Happy App')

        happyAppBuildNotPublishedIdentifier1 = 'happy.build.1'
        createBuild(happyAppId, platform, happyAppBuildNotPublishedIdentifier1)

        happyAppBuildNotPublishedIdentifier2 = 'happy.build.2'
        createBuild(happyAppId, platform, happyAppBuildNotPublishedIdentifier2)
        happyAppProject
    }

    private createSadProjectAndItsAppsAndBuilds() {
        sadAppProject = createProject('Sad App Project')
        sadAppId = 'sad.app.id'

        createApp(sadAppProject, platform, sadAppId, 'Sad App')

        sadAppBuildNotPublishedIdentifier1 = 'sad.build.1'
        createBuild(sadAppId, platform, sadAppBuildNotPublishedIdentifier1)

        sadAppBuildNotPublishedIdentifier2 = 'sad.build.2'
        createBuild(sadAppId, platform, sadAppBuildNotPublishedIdentifier2)
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
        createProject(name, null)
    }

    private Project createProject(String name, String iconHref) {
        setupHelper.createProject(name, iconHref)
    }

    private assignUserToProject(String userEmail, Project project) {
        setupHelper.assignUserToProject(userEmail, project)
    }

    private App createApp(Project project, String platform, String appId, String name) {
        createApp(project, platform, appId, name, null)
    }

    private App createApp(Project project, String platform, String appId, String name, String iconHref) {
        setupHelper.createApp(project, determinePlatformFromText(platform), appId, name, iconHref)
    }

    private createBuild(String appId, String platform, String buildIdentifier) {
        def releaserEmail = 'releaser@shuttle.com'
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

    private publishSadAppBuild() {
        post("/projects/${sadAppProject.id}/apps/${platform}/${sadAppId}/builds/${sadAppBuildNotPublishedIdentifier1}/publish", publisherToken)
    }

}
