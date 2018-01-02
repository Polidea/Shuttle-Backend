package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.user.permissions.PermissionType

import static com.polidea.shuttle.test_config.DefaultAvatarsConfigurationForTests.NEXT_RANDOM_DEFAULT_AVATAR_URL

class AdminUserProjectsManagementControllerSpec extends HttpIntegrationSpecification {

    String adminToken

    void setup() {
        createAdminUser('admin@shuttle.com')
        adminToken = createAccessTokenFor('admin@shuttle.com')
    }

    def "should fetch Users assigned to Project"() {
        given:
        def project = createProject('Mega Project')
        def user1Email = 'first.user@shuttle.com'
        def user2Email = 'second.user@shuttle.com'
        createUser(user1Email, "Name of ${user1Email}")
        createUser(user2Email, "Name of ${user2Email}")
        setupHelper.assignGlobalPermissions(user1Email, [PermissionType.ADMIN])
        setupHelper.assignProjectPermission(project, user1Email, PermissionType.ARCHIVER)

        and:
        def assignment1Response = post("/admin/projects/${project.id}/users/${user1Email}", adminToken)
        assert assignment1Response.code() == 204
        def assignment2Response = post("/admin/projects/${project.id}/users/${user2Email}", adminToken)
        assert assignment2Response.code() == 204

        when:
        def assigneesResponse = get("/admin/projects/${project.id}/users", adminToken)

        then:
        assigneesResponse.code() == 200
        assigneesResponse.body().users as Set == [
                [
                        email             : user1Email,
                        name              : "Name of ${user1Email}",
                        avatarHref        : NEXT_RANDOM_DEFAULT_AVATAR_URL,
                        globalPermissions : ["admin_access"],
                        projectPermissions: ["can_archive"]
                ],
                [
                        email             : user2Email,
                        name              : "Name of ${user2Email}",
                        avatarHref        : NEXT_RANDOM_DEFAULT_AVATAR_URL,
                        globalPermissions : ["can_mute"],
                        projectPermissions: []
                ]
        ] as Set
    }

    def "should fail to fetch Users assigned to non-existent Project"() {
        given:
        def nonExistentProjectId = 123

        when:
        def assigneesResponse = get("/admin/projects/${nonExistentProjectId}/users", adminToken)

        then:
        assigneesResponse.code() == 404
        assigneesResponse.body().code == 2003
        assigneesResponse.body().message == 'Project not found'
    }

    def "should assign User to Project"() {
        given:
        def project = createProject('Mega Project')
        def userEmail = 'any.user@shuttle.com'
        createUser(userEmail, "Name of ${userEmail}")

        when:
        def assignmentResponse = post("/admin/projects/${project.id}/users/${userEmail}", adminToken)

        then:
        assignmentResponse.code() == 204

        and:
        def assigneesResponse = get("/admin/projects/${project.id}/users", adminToken)
        assigneesResponse.code() == 200
        assigneesResponse.body().users*.email == [userEmail]
    }

    def "should fail to assign non-existent User to Project"() {
        given:
        def project = createProject('Mega Project')
        def nonExistentUserEmail = 'no.user@shuttle.com'

        when:
        def assignmentResponse = post("/admin/projects/${project.id}/users/${nonExistentUserEmail}", adminToken)

        then:
        assignmentResponse.code() == 404
        assignmentResponse.body().code == 2001
        assignmentResponse.body().message == "User '${nonExistentUserEmail}' does not exist"
    }

    def "should fail to assign User to non-existent Project"() {
        given:
        def nonExistentProjectId = 123
        def userEmail = 'any.user@shuttle.com'
        createUser(userEmail, "Name of ${userEmail}")

        when:
        def assignmentResponse = post("/admin/projects/${nonExistentProjectId}/users/${userEmail}", adminToken)

        then:
        assignmentResponse.code() == 404
        assignmentResponse.body().code == 2003
        assignmentResponse.body().message == 'Project not found'
    }

    def "should unassign User from Project"() {
        given:
        def project = createProject('Mega Project')
        def userEmail = 'any.user@shuttle.com'
        createUser(userEmail, "Name of ${userEmail}")

        and:
        def assignmentResponse = post("/admin/projects/${project.id}/users/${userEmail}", adminToken)
        assert assignmentResponse.code() == 204

        when:
        def unassignmentResponse = delete("/admin/projects/${project.id}/users/${userEmail}", adminToken)

        then:
        unassignmentResponse.code() == 204

        and:
        def assigneesResponse = get("/admin/projects/${project.id}/users", adminToken)
        assigneesResponse.code() == 200
        assigneesResponse.body().users == []
    }

    def "should unassign User from Project and remove all its project-level permissions"() {
        given:
        def project = createProject('Mega Project')
        def userEmail = 'any.user@shuttle.com'
        createUser(userEmail, "Name of ${userEmail}")

        and:
        def assignmentResponse = post("/admin/projects/${project.id}/users/${userEmail}", adminToken)
        assert assignmentResponse.code() == 204

        and:
        def addCanPublishResponse = post("/admin/projects/${project.id}/users/${userEmail}/permission/can_publish", adminToken)
        assert addCanPublishResponse.code() == 204

        and:
        def addAdminAccessResponse = post("/admin/projects/${project.id}/users/${userEmail}/permission/admin_access", adminToken)
        assert addAdminAccessResponse.code() == 204

        when:
        def unassignmentResponse = delete("/admin/projects/${project.id}/users/${userEmail}", adminToken)

        then:
        unassignmentResponse.code() == 204

        and:
        def assigneesResponse = get("/admin/projects/${project.id}/users", adminToken)
        assigneesResponse.code() == 200
        assigneesResponse.body().users == []

        and:
        post("/admin/projects/${project.id}/users/${userEmail}", adminToken)

        and:
        get("/admin/users/${userEmail}", adminToken)
                .body().projects[0].permissions == []
    }

    def "should fail to unassign non-existent User to Project"() {
        given:
        def project = createProject('Mega Project')
        def nonExistentUserEmail = 'no.user@shuttle.com'

        when:
        def assignmentResponse = delete("/admin/projects/${project.id}/users/${nonExistentUserEmail}", adminToken)

        then:
        assignmentResponse.code() == 404
        assignmentResponse.body().code == 2001
        assignmentResponse.body().message == "User '${nonExistentUserEmail}' does not exist"
    }

    def "should fail to unassign User to non-existent Project"() {
        given:
        def nonExistentProjectId = 123
        def userEmail = 'any.user@shuttle.com'
        createUser(userEmail, "Name of ${userEmail}")

        when:
        def assignmentResponse = delete("/admin/projects/${nonExistentProjectId}/users/${userEmail}", adminToken)

        then:
        assignmentResponse.code() == 404
        assignmentResponse.body().code == 2003
        assignmentResponse.body().message == 'Project not found'
    }

    private void createAdminUser(String adminEmail) {
        setupHelper.createUser(adminEmail, "Name of ${adminEmail}", null)
        setupHelper.assignGlobalPermissions(adminEmail, [PermissionType.ADMIN])
    }

    private String createAccessTokenFor(String userEmail) {
        return setupHelper.createClientAccessToken(userEmail, 'any-device-id')
    }

    private void createUser(String userEmail, String name) {
        setupHelper.createUser(userEmail, name, null)
    }

    private Project createProject(String name) {
        return setupHelper.createProject(name, null)
    }

}
