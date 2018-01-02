package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.permissions.PermissionType
import spock.lang.Unroll

class AdminAppsController_EditApp_HttpSpec extends HttpIntegrationSpecification {

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
    def "should edit App name (new name: #updatedName"(String updatedName) {
        given:
        def createAppResponse = post("/admin/projects/${project.id}/apps/android/fancy-app", [
                name    : 'Fancy App',
                iconHref: 'fancy_icon_href'
        ], adminToken)
        assert createAppResponse.code() == 204

        when:
        def editAppResponse = patch("/admin/projects/${project.id}/apps/android/fancy-app", [
                name: updatedName
        ], adminToken)

        then:
        editAppResponse.code() == 204

        and:
        def appsResponse = get("/admin/projects/${project.id}/apps/android", adminToken)
        appsResponse.code() == 200
        appsResponse.body().apps.size() == 1
        appsResponse.body().apps.find { it.id == 'fancy-app' }.name == updatedName
        appsResponse.body().apps.find { it.id == 'fancy-app' }.iconHref == 'fancy_icon_href'

        where:
        updatedName << ['UPDATED App']
    }

    @Unroll
    def "should fail to edit App name with invalid value (invalid name: #invalidName"(String invalidName) {
        given:
        def createAppResponse = post("/admin/projects/${project.id}/apps/android/fancy-app", [
                name    : 'Fancy App',
                iconHref: 'fancy_icon_href'
        ], adminToken)
        assert createAppResponse.code() == 204

        when:
        def editAppResponse = patch("/admin/projects/${project.id}/apps/android/fancy-app", [
                name: invalidName
        ], adminToken)

        then:
        editAppResponse.code() == 400

        and:
        def appsResponse = get("/admin/projects/${project.id}/apps/android", adminToken)
        appsResponse.code() == 200
        appsResponse.body().apps.size() == 1
        appsResponse.body().apps.find { it.id == 'fancy-app' }.name == 'Fancy App'

        where:
        invalidName << ['', '    ', null]
    }

    @Unroll
    def "should edit App icon (new icon href: #updatedIconHref"(String updatedIconHref) {
        given:
        def createAppResponse = post("/admin/projects/${project.id}/apps/android/fancy-app", [
                name    : 'Fancy App',
                iconHref: 'fancy_icon_href'
        ], adminToken)
        assert createAppResponse.code() == 204

        when:
        def editAppResponse = patch("/admin/projects/${project.id}/apps/android/fancy-app", [
                iconHref: updatedIconHref
        ], adminToken)

        then:
        editAppResponse.code() == 204

        and:
        def appsResponse = get("/admin/projects/${project.id}/apps/android", adminToken)
        appsResponse.code() == 200
        appsResponse.body().apps.size() == 1
        appsResponse.body().apps.find { it.id == 'fancy-app' }.name == 'Fancy App'
        appsResponse.body().apps.find { it.id == 'fancy-app' }.iconHref == updatedIconHref

        where:
        updatedIconHref << ['UPDATED_icon_href', '', null]
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
