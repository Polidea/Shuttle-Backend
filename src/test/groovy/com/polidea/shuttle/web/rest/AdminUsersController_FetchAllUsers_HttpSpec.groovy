package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.user.permissions.PermissionType
import spock.lang.Unroll

import static com.polidea.shuttle.test_config.DefaultAvatarsConfigurationForTests.NEXT_RANDOM_DEFAULT_AVATAR_URL

class AdminUsersController_FetchAllUsers_HttpSpec extends HttpIntegrationSpecification {

    String adminToken
    String moderatorToken

    void setup() {
        adminToken = createAndAuthenticateUser('admin@user.com')
        assignGlobalPermissions('admin@user.com', [PermissionType.ADMIN])

        def project = createProject('Test Project')
        moderatorToken = createAndAuthenticateUser('moderator@user.com')
        assignUserToProject('moderator@user.com', project)
        assignProjectPermission(project, 'moderator@user.com', PermissionType.ADMIN)
    }

    def "Moderator should fetch empty list of users"() {
        given:
        createUser('eve.shy@user.com', 'Eve Shy', null)

        when:
        def usersResponse = get('/admin/users', moderatorToken)

        then:
        usersResponse.code() == 200
        usersResponse.body().users.size() == 0
    }

    def "Moderator should fetch list of one user which has set isVisibleForModerator flag"() {
        given:
        createUser('eve.shy@user.com', 'Eve Shy', null)

        when:
        patch("/admin/users/eve.shy@user.com", [
                isVisibleForModerator: true
        ], adminToken)
        def usersResponse = get('/admin/users', moderatorToken)

        then:
        usersResponse.code() == 200
        usersResponse.body().users.size() == 1
    }

    def "Normal user should not be able to fetch list of users"() {
        given:
        def userToken = createAndAuthenticateUser('adrian@user.com')

        when:
        def usersResponse = get('/admin/users', userToken)

        then:
        usersResponse.code() == 403
    }

    def "should fetch all Users"() {
        given:
        createUser('eve.shy@user.com', 'Eve Shy', null)
        createUser('john.confident@user.com', 'John Confident', 'john_confident_avatar')
        assignGlobalPermissions('john.confident@user.com', [PermissionType.ARCHIVER, PermissionType.PUBLISHER])
        def project = createProject('Mega Project')
        assignUserToProject('john.confident@user.com', project)
        assignProjectPermission(project, 'john.confident@user.com', PermissionType.PUBLISHER)
        assignProjectPermission(project, 'john.confident@user.com', PermissionType.ARCHIVER)

        when:
        def usersResponse = get('/admin/users', adminToken)

        then:
        usersResponse.code() == 200
        usersResponse.body().users.size() == 4

        usersResponse.body().users.count { it.email == 'admin@user.com' } == 1

        def eveShy = usersResponse.body().users.find { it.email == 'eve.shy@user.com' }
        eveShy.name == 'Eve Shy'
        eveShy.avatarHref == NEXT_RANDOM_DEFAULT_AVATAR_URL
        eveShy.globalPermissions == ["can_mute"]
        eveShy.projects == []

        def johnConfident = usersResponse.body().users.find { it.email == 'john.confident@user.com' }
        johnConfident.name == 'John Confident'
        johnConfident.avatarHref == 'john_confident_avatar'
        johnConfident.globalPermissions as Set == ['can_archive', 'can_publish'] as Set
        johnConfident.projects.size() == 1
        johnConfident.projects[0].id == project.id()
        johnConfident.projects[0].name == project.name()
        johnConfident.projects[0].iconHref == project.iconHref()
        johnConfident.projects[0].permissions as Set == ['can_archive', 'can_publish'] as Set
    }

    def "when fetching all Users then should include only projects (and their permissions) to which Users are assigned to"() {
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
        assignProjectPermission(projectC, userEmail, PermissionType.PUBLISHER)

        when:
        def usersResponse = get('/admin/users', adminToken)

        then:
        usersResponse.code() == 200
        usersResponse.body().users.size() == 3

        usersResponse.body().users.count { it.email == 'admin@user.com' } == 1

        def user = usersResponse.body().users.find { it.email == userEmail }
        user.projects.size() == 2
        user.projects.find { it.id == projectA.id() }.permissions == ['can_publish']
        user.projects.find { it.id == projectB.id() }.permissions == []
    }

    @Unroll
    def "should not fetch Users if has no privilege to do so (permission: #nonAdminPermission)"(PermissionType nonAdminPermission) {
        given:
        createUser('non-admin@user.com')
        assignGlobalPermissions('non-admin@user.com', [nonAdminPermission])
        def nonAdminToken = createAccessTokenFor('non-admin@user.com')

        when:
        def usersResponse = get('/admin/users', nonAdminToken)

        then:
        usersResponse.code() == 403

        where:
        nonAdminPermission << PermissionType.values() - PermissionType.ADMIN
    }

    private void createUser(String userEmail) {
        setupHelper.createUser(userEmail, "Name of ${userEmail}", null)
    }

    private void createUser(String userEmail, String name, String avatarHref) {
        setupHelper.createUser(userEmail, name, avatarHref)
    }

    private String createAccessTokenFor(String userEmail) {
        return setupHelper.createClientAccessToken(userEmail, 'any-device-id')
    }

    private String createAndAuthenticateUser(String userEmail) {
        return setupHelper.createAndAuthenticateClientUser(
                userEmail,
                "anyValidAccessTokenForUser_${userEmail}",
                null,
                'any-device-id'
        )
    }

    private Project createProject(String name) {
        setupHelper.createProject(name, 'any_icon_href')
    }

    private void assignUserToProject(String userEmail, Project project) {
        setupHelper.assignUserToProject(userEmail, project.id)
    }

    private void assignGlobalPermissions(String userEmail, List<PermissionType> permissions) {
        setupHelper.assignGlobalPermissions(userEmail, permissions)
    }

    private void assignProjectPermission(Project project, String userEmail, PermissionType permission) {
        setupHelper.assignProjectPermission(project, userEmail, permission)
    }

}
