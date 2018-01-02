package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.user.permissions.PermissionType
import spock.lang.Unroll

class AdminUsersController_FetchUser_HttpSpec extends HttpIntegrationSpecification {

    String adminToken

    void setup() {
        createUser('admin@user.com')
        assignGlobalPermissions('admin@user.com', [PermissionType.ADMIN])
        adminToken = createAccessTokenFor('admin@user.com')
    }

    def "should fetch an User"() {
        given:
        String userEmail = 'john@user.com'
        createUser(userEmail, 'John Confident', 'john_confident_avatar')

        and:
        assignGlobalPermissions(userEmail, [PermissionType.ARCHIVER, PermissionType.PUBLISHER])
        def project = createProject('Mega Project')
        assignUserToProject(userEmail, project)
        assignProjectPermission(project, userEmail, PermissionType.PUBLISHER)

        when:
        def userResponse = get("/admin/users/${userEmail}", adminToken)

        then:
        userResponse.code() == 200
        userResponse.body().email == userEmail
        userResponse.body().name == 'John Confident'
        userResponse.body().avatarHref == 'john_confident_avatar'
        userResponse.body().globalPermissions as Set == ['can_archive', 'can_publish'] as Set
        userResponse.body().projects.size() == 1
        userResponse.body().projects[0].id == project.id()
        userResponse.body().projects[0].name == project.name()
        userResponse.body().projects[0].iconHref == project.iconHref()
        userResponse.body().projects[0].permissions as Set == ['can_publish'] as Set
    }

    def "should fail to fetch an User which does not exist"() {
        when:
        def userResponse = get("/admin/users/john@user.com", adminToken)

        then:
        userResponse.code() == 404
        userResponse.body().code == 2001
        userResponse.body().message == "User 'john@user.com' does not exist"
    }

    @Unroll
    def "should not fetch an User if has no privilege to do so (permission: #nonAdminPermission)"(PermissionType nonAdminPermission) {
        given:
        createUser('non-admin@user.com')
        assignGlobalPermissions('non-admin@user.com', [nonAdminPermission])
        def nonAdminToken = createAccessTokenFor('non-admin@user.com')

        and:
        def userEmail = 'john@user.com'
        createUser(userEmail)

        when:
        def userResponse = get("/admin/users/${userEmail}", nonAdminToken)

        then:
        userResponse.code() == 403

        where:
        nonAdminPermission << PermissionType.values() - PermissionType.ADMIN
    }

    @Unroll
    def "should fetch own User even if has no ADMIN (permission: #nonAdminPermission)"(PermissionType nonAdminPermission) {
        given:
        String userEmail = 'john@user.com'
        createUser(userEmail)

        and:
        assignGlobalPermissions(userEmail, [nonAdminPermission])
        def userToken = createAccessTokenFor(userEmail)

        when:
        def userResponse = get("/admin/users/${userEmail}", userToken)

        then:
        userResponse.code() == 200
        userResponse.body().email == userEmail

        where:
        nonAdminPermission << PermissionType.values() - PermissionType.ADMIN
    }

    def "when fetching an User then should include only projects (and their permissions) to which User is assigned to"() {
        given:
        def userEmail = 'john@user.com'
        createUser(userEmail)

        and:
        def projectA = createProject('Project A - WITH user assigned, WITH permissions specified')
        def projectB = createProject('Project B - WITH user assigned, without permissions specified')
        def projectC = createProject('Project C - without user assigned, WITH permissions specified')
        assignUserToProject(userEmail, projectA)
        assignUserToProject(userEmail, projectB)
        assignProjectPermission(projectA, userEmail, PermissionType.PUBLISHER)
        assignProjectPermission(projectA, userEmail, PermissionType.ADMIN)
        assignProjectPermission(projectC, userEmail, PermissionType.PUBLISHER)
        assignProjectPermission(projectC, userEmail, PermissionType.ADMIN)

        when:
        def userResponse = get("/admin/users/${userEmail}", adminToken)

        then:
        userResponse.code() == 200
        userResponse.body().email == userEmail
        userResponse.body().projects.size() == 2
        userResponse.body().projects.find { it.id == projectA.id() }.permissions as Set ==
                ['admin_access', 'can_publish'] as Set
        userResponse.body().projects.find { it.id == projectB.id() }.permissions == []
    }

    def "should fetch a new User instead of deleted one"() {
        given:
        String userEmail = 'john@user.com'
        def project = createProject('Mega Project')

        and:
        createUser(userEmail, 'John Confident', 'john_confident_avatar')
        assignGlobalPermissions(userEmail, [PermissionType.ARCHIVER])
        assignUserToProject(userEmail, project)
        assignProjectPermission(project, userEmail, PermissionType.ADMIN)

        and:
        deleteUser(userEmail)

        and:
        createUser(userEmail, 'BETTER John', 'BETTER_john_confident')
        assignGlobalPermissions(userEmail, [PermissionType.MUTER])
        assignUserToProject(userEmail, project)
        assignProjectPermission(project, userEmail, PermissionType.PUBLISHER)

        when:
        def userResponse = get("/admin/users/${userEmail}", adminToken)

        then:
        userResponse.code() == 200
        userResponse.body().email == userEmail
        userResponse.body().name == 'BETTER John'
        userResponse.body().avatarHref == 'BETTER_john_confident'
        userResponse.body().globalPermissions == ['can_mute']
        userResponse.body().projects.size() == 1
        userResponse.body().projects[0].id == project.id()
        userResponse.body().projects[0].name == project.name()
        userResponse.body().projects[0].iconHref == project.iconHref()
        userResponse.body().projects[0].permissions == ['can_publish']
    }

    private void createUser(String userEmail) {
        setupHelper.createUser(userEmail, "Name of ${userEmail}", null)
    }

    private void createUser(String userEmail, String name, String avatarHref) {
        setupHelper.createUser(userEmail, name, avatarHref)
    }

    private deleteUser(String userEmail) {
        setupHelper.deleteUser(userEmail)
    }

    private String createAccessTokenFor(String userEmail) {
        return setupHelper.createClientAccessToken(userEmail, 'any-device-id')
    }

    private Project createProject(String name) {
        setupHelper.createProject(name, 'any_icon_href')
    }

    def assignUserToProject(String userEmail, Project project) {
        setupHelper.assignUserToProject(userEmail, project.id)
    }

    private assignGlobalPermissions(String userEmail, List<PermissionType> permissions) {
        setupHelper.assignGlobalPermissions(userEmail, permissions)
    }

    private assignProjectPermission(Project project, String userEmail, PermissionType permission) {
        setupHelper.assignProjectPermission(project, userEmail, permission)
    }

}
