package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.user.permissions.PermissionType

class AdminProjectsController_EditProject_HttpSpec extends HttpIntegrationSpecification {

    String adminToken

    void setup() {
        createUser('admin@user.com')
        assignGlobalPermissions('admin@user.com', [PermissionType.ADMIN])
        adminToken = createAccessTokenFor('admin@user.com')
    }

    def "should edit Project name (new name: #updatedName)"(String updatedName) {
        given:
        def project = createProject('Some Project', 'some_icon_href')

        when:
        def editProjectResponse = patch("/admin/projects/${project.id}", [
                name: updatedName
        ], adminToken)

        then:
        editProjectResponse.code() == 204

        and:
        def projectsResponse = get('/admin/projects', adminToken)
        projectsResponse.code() == 200
        projectsResponse.body().projects.find { it.id == project.id }.name == updatedName
        projectsResponse.body().projects.find { it.id == project.id }.iconHref == 'some_icon_href'

        where:
        updatedName << ['UPDATED Project']
    }

    def "should fail to edit Project name with invalid value (invalid name: #invalidName)"(String invalidName) {
        given:
        def project = createProject('Some Project', 'some_icon_href')

        when:
        def editProjectResponse = patch("/admin/projects/${project.id}", [
                name: invalidName
        ], adminToken)

        then:
        editProjectResponse.code() == 400

        and:
        def projectsResponse = get('/admin/projects', adminToken)
        projectsResponse.code() == 200
        projectsResponse.body().projects.find { it.id == project.id }.name == 'Some Project'

        where:
        invalidName << ['', '    ', null]
    }

    def "should edit Project icon (new icon href: #updatedIconHref)"(String updatedIconHref) {
        given:
        def project = createProject('Some Project', 'some_icon_href')

        when:
        def editProjectResponse = patch("/admin/projects/${project.id}", [
                iconHref: updatedIconHref
        ], adminToken)

        then:
        editProjectResponse.code() == 204

        and:
        def projectsResponse = get('/admin/projects', adminToken)
        projectsResponse.code() == 200
        projectsResponse.body().projects.find { it.id == project.id }.name == 'Some Project'
        projectsResponse.body().projects.find { it.id == project.id }.iconHref == updatedIconHref

        where:
        updatedIconHref << ['UPDATED_icon_href', '', null]
    }

    private void createUser(String userEmail) {
        setupHelper.createUser(userEmail, "Name of ${userEmail}", null)
    }

    private String createAccessTokenFor(String userEmail) {
        return setupHelper.createClientAccessToken(userEmail, 'any-device-id')
    }

    private assignGlobalPermissions(String userEmail, List<PermissionType> permissions) {
        setupHelper.assignGlobalPermissions(userEmail, permissions)
    }

    private Project createProject(String name, String iconHref) {
        return setupHelper.createProject(name, iconHref)
    }

}
