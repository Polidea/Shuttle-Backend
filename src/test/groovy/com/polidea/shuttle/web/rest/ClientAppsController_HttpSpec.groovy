package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.app.App
import com.polidea.shuttle.domain.app.Platform
import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.permissions.PermissionType
import spock.lang.Unroll

import static Platform.ANDROID

class ClientAppsController_HttpSpec extends HttpIntegrationSpecification {

    String clientEmail = 'client@shuttle.com'
    String adminToken
    String clientToken
    Project project
    Project anotherProject

    def setup() {
        createUser('admin@shuttle.com')
        assignGlobalPermissions('admin@shuttle.com', [PermissionType.ADMIN])
        adminToken = createAccessTokenFor('admin@shuttle.com')
        project = createProject('Some Project')
        anotherProject = createProject('Another Project')
        createUser(clientEmail)
        clientToken = createAccessTokenFor(clientEmail)
    }

    def "User cannot fetch Apps for Project which he is not assigned to"() {
        given:
        // User is not assigned to Project

        when:
        def appsResponse = get("/projects/${project.id}/apps/android", clientToken)

        then:
        appsResponse.code() == 403
    }

    def "Project has no Apps by default"() {
        given:
        assignUserToProject(clientEmail, project)

        when:
        def appsResponse = get("/projects/${project.id}/apps/android", clientToken)

        then:
        appsResponse.code() == 200
        appsResponse.body().apps == []
    }

    def "Project has its own Apps"() {
        given:
        assignUserToProject(clientEmail, project)

        and:
        createApp(project, ANDROID, 'app.1')
        createApp(project, ANDROID, 'app.2')
        createApp(anotherProject, ANDROID, 'app.from.another.project')

        when:
        def appsResponse = get("/projects/${this.project.id}/apps/android", clientToken)

        then:
        appsResponse.code() == 200
        appsResponse.body().apps.size() == 2
        appsResponse.body().apps.count { it.id == 'app.1' } == 1
        appsResponse.body().apps.count { it.id == 'app.2' } == 1
        appsResponse.body().apps.count { it.id == 'app.from.another.project' } == 0
    }

    def "deleted User cannot fetch Apps "() {
        given:
        assignUserToProject(clientEmail, project)

        and:
        createApp(project, ANDROID, 'app.1')
        createApp(project, ANDROID, 'app.2')

        and:
        deleteUser(clientEmail)

        when:
        def appsResponse = get("/projects/${this.project.id}/apps/android", clientToken)

        then:
        appsResponse.code() == 401
    }

    def "App can have its own icon"() {
        given:
        assignUserToProject(clientEmail, project)

        and:
        setProjectIcon(project, 'http://project_icon')

        when:
        createApp(project, ANDROID, 'app', 'http://href_of_fancy_icon')

        then:
        def appsResponse = get("/projects/${project.id}/apps/android", clientToken)
        appsResponse.code() == 200
        appsResponse.body().apps[0].iconHref == 'http://href_of_fancy_icon'
    }

    @Unroll
    def "App uses its Project icon (#projectIconHref) if no icon was specified for App"(String projectIconHref) {
        given:
        assignUserToProject(clientEmail, project)

        and:
        setProjectIcon(project, projectIconHref)

        when:
        createApp(project, ANDROID, 'app', null)

        then:
        def appsResponse = get("/projects/${project.id}/apps/android", clientToken)
        appsResponse.code() == 200
        appsResponse.body().apps[0].iconHref == projectIconHref

        where:
        projectIconHref << ['http://project_icon', null]
    }

    def "App uses its own icon after it was set"() {
        given:
        assignUserToProject(clientEmail, project)

        and:
        setProjectIcon(project, 'http://project_icon')

        and:
        def app = createApp(project, ANDROID, 'app', null)

        when:
        setAppIcon(app, 'http://href_of_fancy_icon')

        then:
        def appsResponse = get("/projects/${project.id}/apps/android", clientToken)
        appsResponse.code() == 200
        appsResponse.body().apps[0].iconHref == 'http://href_of_fancy_icon'
    }

    def "App uses its Project icon after App icon was deleted"() {
        given:
        assignUserToProject(clientEmail, project)

        and:
        setProjectIcon(project, 'http://project_icon')

        and:
        def app = createApp(project, ANDROID, 'app', 'http://href_of_fancy_icon')

        when:
        setAppIcon(app, null)

        then:
        def appsResponse = get("/projects/${project.id}/apps/android", clientToken)
        appsResponse.code() == 200
        appsResponse.body().apps[0].iconHref == 'http://project_icon'
    }

    private User createUser(String userEmail) {
        return setupHelper.createUser(userEmail, "Name of ${userEmail}", null)
    }

    private void assignGlobalPermissions(String userEmail, List<PermissionType> permissions) {
        setupHelper.assignGlobalPermissions(userEmail, permissions)
    }

    private String createAccessTokenFor(String userEmail) {
        return setupHelper.createClientAccessToken(userEmail, 'any-device-id')
    }

    private Project createProject(String name) {
        return setupHelper.createProject(name, null)
    }

    private setProjectIcon(Project project, String newIconHref) {
        setupHelper.editProject(project.id(), project.name(), newIconHref)
    }
    private void assignUserToProject(String userEmail, Project project) {
        setupHelper.assignUserToProject(userEmail, project.id)
    }

    private void createApp(Project project, Platform platform, appId) {
        setupHelper.createApp(project, platform, appId, "name of ${appId}", null)
    }

    private App createApp(Project project, Platform platform, appId, String iconHref) {
        return setupHelper.createApp(project, platform, appId, "name of ${appId}", iconHref)
    }

    private setAppIcon(App app, String newIconHref) {
        setupHelper.editApp(app.platform(), app.appId(), app.name(), newIconHref)
    }


    private deleteUser(String userEmail) {
        setupHelper.deleteUser(userEmail)
    }

}

