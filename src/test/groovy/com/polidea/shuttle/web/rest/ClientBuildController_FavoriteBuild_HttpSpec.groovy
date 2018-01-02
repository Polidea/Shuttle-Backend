package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.app.App
import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.permissions.PermissionType

import static com.polidea.shuttle.domain.app.Platform.determinePlatformFromText

class ClientBuildController_FavoriteBuild_HttpSpec extends HttpIntegrationSpecification {

    String userEmail
    String userToken
    Project project
    String appId
    String platform
    String buildIdentifier

    def setup() {
        userEmail = 'client@shuttle.com'
        createUser(userEmail, 'Any Shuttle Client')
        assignGlobalPermission(userEmail, PermissionType.PUBLISHER)
        userToken = createClientAccessTokenFor(userEmail)

        project = createProject('Any Project')
        setupHelper.assignUserToProject(userEmail, project)

        appId = 'any.app.id'
        platform = 'ios'
        createApp(project, platform, appId, 'Any App')

        buildIdentifier = 'ios.prefix.schema.123'
        createBuild(appId, platform, buildIdentifier)
    }

    def "Build is not favorite by default"() {
        when:
        def buildsResponse = get("/projects/${project.id}/apps/${platform}/${appId}/builds", userToken)
        assert buildsResponse.code() == 200

        then:
        buildsResponse.body().builds.size() == 1
        buildsResponse.body().builds[0].isFavorite == false
    }

    def "favorite Build"() {
        when:
        def favoriteResponse = post("/projects/${project.id}/apps/${platform}/${appId}/builds/${buildIdentifier}/favorite", userToken)

        then:
        favoriteResponse.code() == 204

        and:
        def buildsResponse = get("/projects/${project.id}/apps/${platform}/${appId}/builds", userToken)
        buildsResponse.code() == 200
        buildsResponse.body().builds.size() == 1
        buildsResponse.body().builds[0].isFavorite == true
    }

    def "unfavorite Build"() {
        given:
        def favoriteResponse = post("/projects/${project.id}/apps/${platform}/${appId}/builds/${buildIdentifier}/favorite", userToken)
        assert favoriteResponse.code() == 204

        when:
        def unfavoriteResponse = delete("/projects/${project.id}/apps/${platform}/${appId}/builds/${buildIdentifier}/favorite", userToken)

        then:
        assert unfavoriteResponse.code() == 204

        and:
        def buildsResponse = get("/projects/${project.id}/apps/${platform}/${appId}/builds", userToken)
        buildsResponse.code() == 200
        buildsResponse.body().builds.size() == 1
        buildsResponse.body().builds[0].isFavorite == false
    }

    def "cannot favorite Build if User is not assigned to Project"() {
        given:
        unassignUserFromProject(userEmail, project)

        when:
        def favoriteResponse = post("/projects/${project.id}/apps/${platform}/${appId}/builds/${buildIdentifier}/favorite", userToken)

        then:
        favoriteResponse.code() == 403
    }

    def "cannot unfavorite Build if User is not assigned to Project"() {
        given:
        unassignUserFromProject(userEmail, project)

        when:
        def favoriteResponse = delete("/projects/${project.id}/apps/${platform}/${appId}/builds/${buildIdentifier}/favorite", userToken)

        then:
        favoriteResponse.code() == 403
    }

    def "cannot favorite Build if Project does not exist"() {
        given:
        def idOfNonExistentProject = 987

        when:
        def favoriteResponse = post("/projects/${idOfNonExistentProject}/apps/${platform}/${appId}/builds/${buildIdentifier}/favorite", userToken)

        then:
        favoriteResponse.code() == 404
        favoriteResponse.body().code == 2003
        favoriteResponse.body().message == 'Project not found'
    }

    def "cannot unfavorite Build if Project does not exist"() {
        given:
        def idOfNonExistentProject = 987

        when:
        def favoriteResponse = delete("/projects/${idOfNonExistentProject}/apps/${platform}/${appId}/builds/${buildIdentifier}/favorite", userToken)

        then:
        favoriteResponse.code() == 404
        favoriteResponse.body().code == 2003
        favoriteResponse.body().message == 'Project not found'
    }

    def "cannot favorite Build if App does not exist"() {
        given:
        def idOfNonExistentApp = 'non.existent.app.id'

        when:
        def favoriteResponse = post("/projects/${project.id}/apps/${platform}/${idOfNonExistentApp}/builds/${buildIdentifier}/favorite", userToken)

        then:
        favoriteResponse.code() == 404
        favoriteResponse.body().code == 2004
        favoriteResponse.body().message ==
                "App '${idOfNonExistentApp}' for platform '${determinePlatformFromText(platform)}' was not found"
    }

    def "cannot unfavorite Build if App does not exist"() {
        given:
        def idOfNonExistentApp = 'non.existent.app.id'

        when:
        def favoriteResponse = delete("/projects/${project.id}/apps/${platform}/${idOfNonExistentApp}/builds/${buildIdentifier}/favorite", userToken)

        then:
        favoriteResponse.code() == 404
        favoriteResponse.body().code == 2004
        favoriteResponse.body().message ==
                "App '${idOfNonExistentApp}' for platform '${determinePlatformFromText(platform)}' was not found"
    }

    def "cannot favorite Build if Build does not exist"() {
        given:
        def identifierOfNonExistentBuild = 'non.existent.build.identifier'

        when:
        def favoriteResponse = post("/projects/${project.id}/apps/${platform}/${appId}/builds/${identifierOfNonExistentBuild}/favorite", userToken)

        then:
        favoriteResponse.code() == 404
        favoriteResponse.body().code == 2005
        favoriteResponse.body().message == "Build with identifier '${identifierOfNonExistentBuild}' was not found"
    }

    def "cannot unfavorite Build if Build does not exist"() {
        given:
        def identifierOfNonExistentBuild = 'non.existent.build.identifier'

        when:
        def favoriteResponse = delete("/projects/${project.id}/apps/${platform}/${appId}/builds/${identifierOfNonExistentBuild}/favorite", userToken)

        then:
        favoriteResponse.code() == 404
        favoriteResponse.body().code == 2005
        favoriteResponse.body().message == "Build with identifier '${identifierOfNonExistentBuild}' was not found"
    }

    def "cannot favorite Build if App exists but in another project"() {
        given:
        def anotherProject = createProject('Another Project')
        assignUserToProject(userEmail, anotherProject)

        when:
        def favoriteResponse = post("/projects/${anotherProject.id}/apps/${platform}/${appId}/builds/${buildIdentifier}/favorite", userToken)

        then:
        favoriteResponse.code() == 404
        favoriteResponse.body().code == 2011
        favoriteResponse.body().message ==
                "App '${appId}' for platform '${determinePlatformFromText(platform)}' was not found in project of ID '${anotherProject.id}'"
    }

    def "cannot unfavorite Build if App exists but in another project"() {
        given:
        def anotherProject = createProject('Another Project')
        setupHelper.assignUserToProject(userEmail, anotherProject)

        when:
        def favoriteResponse = delete("/projects/${anotherProject.id}/apps/${platform}/${appId}/builds/${buildIdentifier}/favorite", userToken)

        then:
        favoriteResponse.code() == 404
        favoriteResponse.body().code == 2011
        favoriteResponse.body().message ==
                "App '${appId}' for platform '${determinePlatformFromText(platform)}' was not found in project of ID '${anotherProject.id}'"
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

    private App createApp(Project project, String platform, String appId, String name) {
        setupHelper.createApp(project, determinePlatformFromText(platform), appId, name, null)
    }

    private createBuild(String appId, String platform, String buildIdentifier) {
        def releaserEmail = 'releaser@shuttle.com'
        createUser(releaserEmail, 'Build Releaser')
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

    private assignUserToProject(String userEmail, Project project) {
        setupHelper.assignUserToProject(userEmail, project)
    }

    private unassignUserFromProject(String userEmail, Project project) {
        setupHelper.unassignUserFromProject(userEmail, project)
    }

}
