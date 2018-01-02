package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.user.permissions.PermissionType

import static org.hamcrest.Matchers.containsInAnyOrder
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AdminProjectPermissionsControllerSpec extends MockMvcIntegrationSpecification {

    String adminToken
    Project testProject

    @Override
    void setup() {
        createUser('admin@user.com')
        assignGlobalPermissions('admin@user.com', [PermissionType.ADMIN])
        adminToken = createAccessTokenFor('admin@user.com')
        testProject = createProject('Test Project 1')
    }

    def "User should have no Project Permissions by default"() {
        given:
        def userEmail = 'john@user.com'
        createUser(userEmail)
        assignUserToProject(userEmail, testProject)

        expect:
        get("/admin/users/${userEmail}", adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.projects[0].permissions').isEmpty())
    }

    def "should add Project Permission"() {
        given:
        def userEmail = 'john@user.com'
        createUser(userEmail)
        assignUserToProject(userEmail, testProject)

        when:
        def result = post("/admin/projects/${testProject.id}/users/${userEmail}/permission/can_publish", adminToken)

        then:
        result.andExpect(status().isNoContent())

        and:
        get("/admin/users/${userEmail}", adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.projects[0].permissions')
                .value(containsInAnyOrder('can_publish')))
    }

    def "should remove existing Project Permission"() {
        given:
        def userEmail = 'john@user.com'
        createUser(userEmail)
        assignUserToProject(userEmail, testProject)
        setupHelper.assignProjectPermission(testProject, userEmail, PermissionType.PUBLISHER)

        when:
        def result = delete("/admin/projects/${testProject.id}/users/${userEmail}/permission/can_publish", adminToken)

        then:
        result.andExpect(status().isNoContent())

        and:
        get("/admin/users/${userEmail}", adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.projects[0].permissions').isEmpty())
    }

    private void createUser(String userEmail) {
        setupHelper.createUser(userEmail, "Name of ${userEmail}", null)
    }

    private String createAccessTokenFor(String userEmail) {
        return setupHelper.createClientAccessToken(userEmail, 'any-device-id')
    }

    private Project createProject(String name) {
        return setupHelper.createProject(name, 'any_icon_href')
    }

    private assignUserToProject(String userEmail, Project project) {
        setupHelper.assignUserToProject(userEmail, project.id)
    }

    private assignGlobalPermissions(String userEmail, List<PermissionType> permissions) {
        setupHelper.assignGlobalPermissions(userEmail, permissions)
    }

}
