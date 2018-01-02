package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.permissions.PermissionType
import spock.lang.Unroll

class AdminAppsController_HttpSpec extends HttpIntegrationSpecification {

    Project project
    String adminToken

    def setup() {
        createUser('admin@shuttle.com')
        assignGlobalPermissions('admin@shuttle.com', [PermissionType.ADMIN])
        adminToken = createAccessTokenFor('admin@shuttle.com')
        project = createProject('Mega Project')
        assignUserToProject('admin@shuttle.com', project)
    }

    @Unroll
    def "should create an App (platform: #platfrom)"(String platform) {
        when:
        def createAppResponse = post("/admin/projects/${project.id}/apps/${platform}/fancy-app", [
                name    : 'Fancy App',
                iconHref: 'fancy_icon_href'
        ], adminToken)

        then:
        createAppResponse.code() == 204

        and:
        def appsResponse = get("/admin/projects/${project.id}/apps/${platform}", adminToken)
        appsResponse.code() == 200
        appsResponse.body().apps.size() == 1
        appsResponse.body().apps[0].id == 'fancy-app'
        appsResponse.body().apps[0].name == 'Fancy App'
        appsResponse.body().apps[0].iconHref == 'fancy_icon_href'
        appsResponse.body().apps[0].lastReleaseDate == null

        where:
        platform << ['android', 'ios']
    }

    def "should create an App without icon href specified"() {
        when:
        def createAppResponse = post("/admin/projects/${project.id}/apps/android/fancy-app", [
                iconHref: 'fancy_icon_href'
        ], adminToken)

        then:
        createAppResponse.code() == 400
    }


    def "should fail to create an App without name specified"() {
        when:
        def createAppResponse = post("/admin/projects/${project.id}/apps/android/fancy-app", [
                name: 'Fancy App'
        ], adminToken)

        then:
        createAppResponse.code() == 204

        and:
        def appsResponse = get("/admin/projects/${project.id}/apps/android", adminToken)
        appsResponse.code() == 200
        appsResponse.body().apps.size() == 1
        appsResponse.body().apps[0].id == 'fancy-app'
        appsResponse.body().apps[0].name == 'Fancy App'
        appsResponse.body().apps[0].iconHref == null
    }

    def "should not create an App for invalid platform"() {
        given:
        def appId = 'fancy-app'
        def invalidPlatform = ' windows'

        when:
        def createAppResponse = post("/admin/projects/${project.id}/apps/${invalidPlatform}/${appId}", [
                name: 'Fancy App'
        ], adminToken)

        then:
        createAppResponse.code() == 400
    }

    def "should delete an App"() {
        given:
        def platform = 'android'
        def appId = 'fancy-app'

        and:
        def createAppResponse = post("/admin/projects/${project.id}/apps/${platform}/${appId}", [
                name: 'Fancy App'
        ], adminToken)
        assert createAppResponse.code() == 204

        when:
        def deleteAppResponse = delete("/admin/projects/${project.id}/apps/${platform}/${appId}", adminToken)

        then:
        deleteAppResponse.code() == 204


        and:
        def appsResponse = get("/admin/projects/${project.id}/apps/${platform}", adminToken)
        appsResponse.code() == 200
        appsResponse.body().apps.size() == 0
    }

    def "should create an App again after deletion"() {
        given:
        def platform = 'android'
        def appId = 'fancy-app'

        and:
        def createAppResponse = post("/admin/projects/${project.id}/apps/${platform}/${appId}", [
                name: 'Fancy App'
        ], adminToken)
        assert createAppResponse.code() == 204

        and:
        def deleteAppResponse = delete("/admin/projects/${project.id}/apps/${platform}/${appId}", adminToken)
        assert deleteAppResponse.code() == 204

        when:
        def createAppAgainResponse = post("/admin/projects/${project.id}/apps/${platform}/${appId}", [
                name: 'Fancy App'
        ], adminToken)

        then:
        createAppAgainResponse.code() == 204

        and:
        def appsResponse = get("/admin/projects/${project.id}/apps/${platform}", adminToken)
        appsResponse.code() == 200
        appsResponse.body().apps.size() == 1
        appsResponse.body().apps[0].id == 'fancy-app'
    }

    def "should not strip word after last dot in App ID when creating an App"() {
        given:
        def appId = 'appIdWithWord.afterDot'

        when:
        def createAppResponse = post("/admin/projects/${project.id}/apps/android/${appId}", [
                name: 'Fancy App'
        ], adminToken)

        then:
        createAppResponse.code() == 204

        and:
        def appsResponse = get("/admin/projects/${project.id}/apps/android", adminToken)
        appsResponse.code() == 200
        appsResponse.body().apps.size() == 1
        appsResponse.body().apps[0].id == appId
    }

    def "should not strip word after last dot in App ID when editing an App"() {
        given:
        def appId = 'appIdWithWord.afterDot'

        and:
        def createAppResponse = post("/admin/projects/${project.id}/apps/android/${appId}", [
                name: 'Fancy App'
        ], adminToken)
        assert createAppResponse.code() == 204

        when:
        def editAppResponse = patch("/admin/projects/${project.id}/apps/android/${appId}", [
                name: 'Updated App'
        ], adminToken)

        then:
        editAppResponse.code() == 204

        and:
        def appsResponse = get("/admin/projects/${project.id}/apps/android", adminToken)
        appsResponse.code() == 200
        appsResponse.body().apps.size() == 1
        appsResponse.body().apps[0].id == appId
        appsResponse.body().apps[0].name == 'Updated App'
    }

    def "should not strip word after last dot in App ID when deleting an App"() {
        given:
        def appId = 'appIdWithWord.afterDot'

        and:
        def createAppResponse = post("/admin/projects/${project.id}/apps/android/${appId}", [
                name: 'Fancy App'
        ], adminToken)
        assert createAppResponse.code() == 204

        when:
        def deleteAppResponse = delete("/admin/projects/${project.id}/apps/android/${appId}", adminToken)

        then:
        deleteAppResponse.code() == 204

        and:
        def appsResponse = get("/admin/projects/${project.id}/apps/android", adminToken)
        appsResponse.code() == 200
        appsResponse.body().apps.size() == 0
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
