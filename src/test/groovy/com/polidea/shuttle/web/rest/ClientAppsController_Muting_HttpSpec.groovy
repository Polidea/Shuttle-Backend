package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.app.Platform
import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.permissions.PermissionType

class ClientAppsController_Muting_HttpSpec extends HttpIntegrationSpecification {

    Project project
    String adminToken

    def setup() {
        createUser('admin@shuttle.com')
        assignGlobalPermissions('admin@shuttle.com', [PermissionType.ADMIN, PermissionType.MUTER])
        adminToken = createAccessTokenFor('admin@shuttle.com')
        project = createProject('Mega Project')
        assignUserToProject('admin@shuttle.com', project)
    }

    def "Project without apps should not be muted"() {
        expect:
        def response = get("/projects", adminToken)
        response.code() == 200
        response.body().projects.size() == 1
        response.body().projects[0].isMuted == false
    }

    def "When all apps are muted then project is muted"() {
        given:
        setupHelper.createApp(project, Platform.ANDROID, "app.id", "app.name", "http://a.com/i.png")

        when:
        post("/projects/${project.id}/apps/android/app.id/mute", adminToken)

        then:
        def projectsResponse = get("/projects", adminToken)
        projectsResponse.code() == 200
        projectsResponse.body().projects.size() == 1
        projectsResponse.body().projects[0].isMuted == true

        and:
        def appsResponse = get("/projects/${project.id}/apps/android", adminToken)
        appsResponse.code() == 200
        appsResponse.body().apps.size() == 1
        appsResponse.body().apps[0].isMuted == true
    }

    def "Not all apps are muted then project is not muted"() {
        given:
        setupHelper.createApp(project, Platform.ANDROID, "app.id1", "app.name", "http://a.com/i.png")
        setupHelper.createApp(project, Platform.ANDROID, "app.id2", "app.name", "http://a.com/i.png")

        when:
        post("/projects/${project.id}/apps/android/app.id1/mute", adminToken)

        then:
        def projectsResponse = get("/projects", adminToken)
        projectsResponse.code() == 200
        projectsResponse.body().projects.size() == 1
        projectsResponse.body().projects[0].isMuted == false

        and:
        def appsResponse = get("/projects/${project.id}/apps/android", adminToken)
        appsResponse.code() == 200
        appsResponse.body().apps.size() == 2
        appsResponse.body().apps.find { it.id == "app.id1" }.isMuted == true
    }

    def "After deleting not muted app when all were muted then project becomes muted"() {
        given:
        setupHelper.createApp(project, Platform.ANDROID, "app.id1", "app.name", "http://a.com/i.png")
        setupHelper.createApp(project, Platform.ANDROID, "app.id2", "app.name", "http://a.com/i.png")

        when:
        post("/projects/${project.id}/apps/android/app.id1/mute", adminToken)
        delete("/admin/projects/${project.id}/apps/android/app.id2/", adminToken)

        then:
        def appsResponse = get("/projects/${project.id}/apps/android", adminToken)
        appsResponse.code() == 200
        appsResponse.body().apps.size() == 1
        appsResponse.body().apps.find { it.id == "app.id1" }.isMuted == true

        and:
        def projectResponseAfterDeletion = get("/projects", adminToken)
        projectResponseAfterDeletion.code() == 200
        projectResponseAfterDeletion.body().projects.size() == 1
        projectResponseAfterDeletion.body().projects[0].isMuted == true
    }

    def "After deleting muted app when all were not muted then project becomes not muted"() {
        given:
        setupHelper.createApp(project, Platform.ANDROID, "app.id1", "app.name", "http://a.com/i.png")
        setupHelper.createApp(project, Platform.ANDROID, "app.id2", "app.name", "http://a.com/i.png")

        when:
        post("/projects/${project.id}/apps/android/app.id2/mute", adminToken)

        then:
        def projectsResponse = get("/projects", adminToken)
        projectsResponse.code() == 200
        projectsResponse.body().projects.size() == 1
        projectsResponse.body().projects[0].isMuted == false

        and:
        delete("/admin/projects/${project.id}/apps/android/app.id2/", adminToken)

        and:
        def projectResponseAfterDeletion = get("/projects", adminToken)
        projectResponseAfterDeletion.code() == 200
        projectResponseAfterDeletion.body().projects.size() == 1
        projectResponseAfterDeletion.body().projects[0].isMuted == false

        and:
        def appsResponse = get("/projects/${project.id}/apps/android", adminToken)
        appsResponse.code() == 200
        appsResponse.body().apps.size() == 1
        appsResponse.body().apps.find { it.id == "app.id1" }.isMuted == false
    }

    def "When all apps become muted then project becomes muted"() {
        given:
        setupHelper.createApp(project, Platform.ANDROID, "app.id1", "app.name", "http://a.com/i.png")
        setupHelper.createApp(project, Platform.ANDROID, "app.id2", "app.name", "http://a.com/i.png")

        when:
        post("/projects/${project.id}/apps/android/app.id1/mute", adminToken)

        then:
        def projectsResponse = get("/projects", adminToken)
        projectsResponse.code() == 200
        projectsResponse.body().projects.size() == 1
        projectsResponse.body().projects[0].isMuted == false

        and:
        post("/projects/${project.id}/apps/android/app.id2/mute", adminToken)

        and:
        def projectResponseAfterMuting = get("/projects", adminToken)
        projectResponseAfterMuting.code() == 200
        projectResponseAfterMuting.body().projects.size() == 1
        projectResponseAfterMuting.body().projects[0].isMuted == true

        and:
        def appsResponse = get("/projects/${project.id}/apps/android", adminToken)
        appsResponse.code() == 200
        appsResponse.body().apps.size() == 2
        appsResponse.body().apps.find { it.id == "app.id1" }.isMuted == true
        appsResponse.body().apps.find { it.id == "app.id2" }.isMuted == true
    }

    def "When all apps become not muted then project becomes not muted"() {
        given:
        setupHelper.createApp(project, Platform.ANDROID, "app.id1", "app.name", "http://a.com/i.png")
        setupHelper.createApp(project, Platform.ANDROID, "app.id2", "app.name", "http://a.com/i.png")

        when:
        post("/projects/${project.id}/apps/android/app.id1/mute", adminToken)

        then:
        def projectsResponse = get("/projects", adminToken)
        projectsResponse.code() == 200
        projectsResponse.body().projects.size() == 1
        projectsResponse.body().projects[0].isMuted == false

        and:
        delete("/projects/${project.id}/apps/android/app.id1/mute", adminToken)

        and:
        def projectResponseAfterMuting = get("/projects", adminToken)
        projectResponseAfterMuting.code() == 200
        projectResponseAfterMuting.body().projects.size() == 1
        projectResponseAfterMuting.body().projects[0].isMuted == false

        and:
        def appsResponse = get("/projects/${project.id}/apps/android", adminToken)
        appsResponse.code() == 200
        appsResponse.body().apps.size() == 2
        appsResponse.body().apps.find { it.id == "app.id1" }.isMuted == false
        appsResponse.body().apps.find { it.id == "app.id2" }.isMuted == false
    }

    def "When all apps are muted and new app was added then project is not muted"() {
        given:
        setupHelper.createApp(project, Platform.ANDROID, "app.id1", "app.name", "http://a.com/i.png")

        when:
        post("/projects/${project.id}/apps/android/app.id1/mute", adminToken)

        then:
        def projectsResponse = get("/projects", adminToken)
        projectsResponse.code() == 200
        projectsResponse.body().projects.size() == 1
        projectsResponse.body().projects[0].isMuted == true

        and:
        setupHelper.createApp(project, Platform.ANDROID, "app.id2", "app.name", "http://a.com/i.png")

        and:
        def projectResponseAfterAddingNewApp = get("/projects", adminToken)
        projectResponseAfterAddingNewApp.code() == 200
        projectResponseAfterAddingNewApp.body().projects.size() == 1
        projectResponseAfterAddingNewApp.body().projects[0].isMuted == false
    }

    def "When all apps are not muted and new app was added then project is not muted"() {
        when:
        setupHelper.createApp(project, Platform.ANDROID, "app.id1", "app.name", "http://a.com/i.png")
        setupHelper.createApp(project, Platform.ANDROID, "app.id2", "app.name", "http://a.com/i.png")

        and:
        setupHelper.createApp(project, Platform.ANDROID, "app.id3", "app.name", "http://a.com/i.png")

        then:
        def projectResponseAfterAddingNewApp = get("/projects", adminToken)
        projectResponseAfterAddingNewApp.code() == 200
        projectResponseAfterAddingNewApp.body().projects.size() == 1
        projectResponseAfterAddingNewApp.body().projects[0].isMuted == false
    }

    def "Project muting mutes all apps when they were not muted"() {
        given:
        setupHelper.createApp(project, Platform.ANDROID, "app.id1", "app.name", "http://a.com/i.png")
        setupHelper.createApp(project, Platform.ANDROID, "app.id2", "app.name", "http://a.com/i.png")

        when:
        post("/projects/${project.id}/mute", adminToken)

        then:
        def response = get("/projects", adminToken)
        response.code() == 200
        response.body().projects.size() == 1
        response.body().projects[0].isMuted == true

        and:
        def appsResponseAfterProjectMuting = get("/projects/${project.id}/apps/android", adminToken)
        appsResponseAfterProjectMuting.code() == 200
        appsResponseAfterProjectMuting.body().apps.size() == 2
        appsResponseAfterProjectMuting.body().apps.find { it.id == "app.id1" }.isMuted == true
        appsResponseAfterProjectMuting.body().apps.find { it.id == "app.id2" }.isMuted == true
    }

    def "Project unmuting unmutes all apps when they were muted"() {
        given:
        setupHelper.createApp(project, Platform.ANDROID, "app.id1", "app.name", "http://a.com/i.png")
        setupHelper.createApp(project, Platform.ANDROID, "app.id2", "app.name", "http://a.com/i.png")

        and:
        post("/projects/${project.id}/apps/android/app.id1/mute", adminToken)
        post("/projects/${project.id}/apps/android/app.id2/mute", adminToken)

        when:
        delete("/projects/${project.id}/mute", adminToken)

        then:
        def projectsResponse = get("/projects", adminToken)
        projectsResponse.body().projects.size() == 1
        projectsResponse.body().projects[0].isMuted == false

        and:
        def appsResponseAfterProjectUnmuting = get("/projects/${project.id}/apps/android", adminToken)
        appsResponseAfterProjectUnmuting.code() == 200
        appsResponseAfterProjectUnmuting.body().apps.size() == 2
        appsResponseAfterProjectUnmuting.body().apps.find { it.id == "app.id1" }.isMuted == false
        appsResponseAfterProjectUnmuting.body().apps.find { it.id == "app.id2" }.isMuted == false
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

    private void assignUserToProject(String userEmail, Project project) {
        setupHelper.assignUserToProject(userEmail, project.id)
    }

}

