package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.app.App
import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.permissions.PermissionType
import spock.lang.Unroll

import static com.polidea.shuttle.domain.app.Platform.determinePlatformFromText

class AdminBuildController_UnpublishBuild_HttpSpec extends HttpIntegrationSpecification {

    String publisherToken
    Project project
    String appId
    String platform
    String buildIdentifier

    def setup() {
        createUser('any.publisher@shuttle.com', 'Publisher')
        assignGlobalPermission('any.publisher@shuttle.com', PermissionType.PUBLISHER)
        publisherToken = createClientAccessTokenFor('any.publisher@shuttle.com')

        project = createProject('Any Project')

        appId = 'any.app.id'
        platform = 'ios'
        createApp(project, platform, appId, 'Any App')

        buildIdentifier = 'ios.prefix.schema.123'
        createBuild(appId, platform, buildIdentifier)
    }

    def "unpublish Build"() {
        given:
        def publishResponse = post("/admin/projects/${project.id}/apps/${platform}/${appId}/builds/${buildIdentifier}/publish", publisherToken)
        assert publishResponse.code() == 204

        when:
        def unpublishResponse = delete("/admin/projects/${project.id}/apps/${platform}/${appId}/builds/${buildIdentifier}/publish", publisherToken)

        then:
        assert unpublishResponse.code() == 204

        and:
        def buildsResponse = get("/admin/projects/${project.id}/apps/${platform}/${appId}/builds", publisherToken)
        buildsResponse.code() == 200
        buildsResponse.body().builds.size() == 1
        buildsResponse.body().builds[0].isPublished == false
    }

    def "cannot unpublish Build if Project does not exist"() {
        given:
        def idOfNonExistentProject = 987

        when:
        def publishResponse = delete("/admin/projects/${idOfNonExistentProject}/apps/${platform}/${appId}/builds/${buildIdentifier}/publish", publisherToken)

        then:
        publishResponse.code() == 404
        publishResponse.body().code == 2003
        publishResponse.body().message == 'Project not found'
    }

    def "cannot unpublish Build if App does not exist"() {
        given:
        def idOfNonExistentApp = 'non.existent.app.id'

        when:
        def publishResponse = delete("/admin/projects/${project.id}/apps/${platform}/${idOfNonExistentApp}/builds/${buildIdentifier}/publish", publisherToken)

        then:
        publishResponse.code() == 404
        publishResponse.body().code == 2004
        publishResponse.body().message ==
                "App '${idOfNonExistentApp}' for platform '${determinePlatformFromText(platform)}' was not found"
    }

    def "cannot unpublish Build if Build does not exist"() {
        given:
        def identifierOfNonExistentBuild = 'non.existent.build.identifier'

        when:
        def publishResponse = delete("/admin/projects/${project.id}/apps/${platform}/${appId}/builds/${identifierOfNonExistentBuild}/publish", publisherToken)

        then:
        publishResponse.code() == 404
        publishResponse.body().code == 2005
        publishResponse.body().message == "Build with identifier '${identifierOfNonExistentBuild}' was not found"
    }

    def "cannot unpublish Build if App exists but in another project"() {
        given:
        def anotherProject = createProject('Another Project')

        when:
        def publishResponse = delete("/admin/projects/${anotherProject.id}/apps/${platform}/${appId}/builds/${buildIdentifier}/publish", publisherToken)

        then:
        publishResponse.code() == 404
        publishResponse.body().code == 2011
        publishResponse.body().message ==
                "App '${appId}' for platform '${determinePlatformFromText(platform)}' was not found in project of ID '${anotherProject.id}'"
    }

    def "User can unpublish Build if has Global Permission to do so"() {
        given:
        createUser('publisher@shuttle.com', 'Totally Not a Publisher')
        def publisherToken = createClientAccessTokenFor('publisher@shuttle.com')
        assignGlobalPermission('publisher@shuttle.com', PermissionType.PUBLISHER)

        when:
        def publishResponse = delete("/admin/projects/${project.id}/apps/${platform}/${appId}/builds/${buildIdentifier}/publish", publisherToken)

        then:
        publishResponse.code() == 204
    }

    @Unroll
    def "User cannot unpublish Build if has no Global Permission to do so (globalPermission: #globalPermission)"(PermissionType globalPermission) {
        given:
        createUser('non.publisher@shuttle.com', 'Totally Not a Publisher')
        def nonPublisherToken = createClientAccessTokenFor('non.publisher@shuttle.com')
        assignGlobalPermission('non.publisher@shuttle.com', globalPermission)

        when:
        def publishResponse = delete("/admin/projects/${project.id}/apps/${platform}/${appId}/builds/${buildIdentifier}/publish", nonPublisherToken)

        then:
        publishResponse.code() == 403

        where:
        globalPermission << PermissionType.values() - PermissionType.PUBLISHER
    }

    def "User can unpublish Build if has Project Permission to do so"() {
        given:
        createUser('publisher@shuttle.com', 'Totally Not a Publisher')
        def publisherToken = createClientAccessTokenFor('publisher@shuttle.com')
        assignUserToProject('publisher@shuttle.com', project)
        assignProjectPermission(project, 'publisher@shuttle.com', PermissionType.PUBLISHER)

        when:
        def publishResponse = delete("/admin/projects/${project.id}/apps/${platform}/${appId}/builds/${buildIdentifier}/publish", publisherToken)

        then:
        publishResponse.code() == 204
    }

    @Unroll
    def "User cannot unpublish Build if has no Project Permission to do so (projectPermission: #projectPermission)"(PermissionType projectPermission) {
        given:
        createUser('non.publisher@shuttle.com', 'Totally Not a Publisher')
        def nonPublisherToken = createClientAccessTokenFor('non.publisher@shuttle.com')
        assignUserToProject('non.publisher@shuttle.com', project)
        assignProjectPermission(project, 'non.publisher@shuttle.com', projectPermission)

        when:
        def publishResponse = delete("/admin/projects/${project.id}/apps/${platform}/${appId}/builds/${buildIdentifier}/publish", nonPublisherToken)

        then:
        publishResponse.code() == 403

        where:
        projectPermission << PermissionType.values() - PermissionType.PUBLISHER
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

    private assignProjectPermission(Project project, String userEmail, PermissionType projectPermission) {
        setupHelper.assignProjectPermission(project, userEmail, projectPermission)
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

}
