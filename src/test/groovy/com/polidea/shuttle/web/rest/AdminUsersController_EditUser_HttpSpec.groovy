package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.user.permissions.PermissionType
import spock.lang.Unroll

class AdminUsersController_EditUser_HttpSpec extends HttpIntegrationSpecification {

    String adminToken
    String userToken

    void setup() {
        createUser('admin@user.com')
        assignGlobalPermissions('admin@user.com', [PermissionType.ADMIN])
        adminToken = createAccessTokenFor('admin@user.com')

        createUser('user@user.com')
        userToken = createAccessTokenFor('user@user.com')
    }

    def "Normal user should not be able to edit his isVisibleForModerator flag"() {
        when:
        def editUserResponse = patch('/admin/users/user@user.com', [
                isVisibleForModerator: true
        ], userToken)

        then:
        editUserResponse.code() == 403
    }

    def "Admin should be able to edit isVisibleForModerator flag"() {
        when:
        def editUserResponse = patch('/admin/users/user@user.com', [
                isVisibleForModerator: true
        ], adminToken)

        then:
        editUserResponse.code() == 204
    }

    @Unroll
    def "should edit an User's name (new name: #updatedName)"(String updatedName) {
        given:
        def userEmail = 'john@user.com'
        createUser(userEmail, 'Just John', 'avatar_of_john')

        when:
        def editUserResponse = patch("/admin/users/${userEmail}", [
                name: updatedName
        ], adminToken)

        then:
        editUserResponse.code() == 204

        and:
        def usersResponse = get("/admin/users/${userEmail}", adminToken)
        usersResponse.code() == 200
        usersResponse.body().name == updatedName
        usersResponse.body().avatarHref == 'avatar_of_john'

        where:
        updatedName << ['John UPDATED']
    }

    @Unroll
    def "should fail to edit an User's name with invalid value (invalid name: #invalidName)"(String invalidName) {
        given:
        def userEmail = 'john@user.com'
        createUser(userEmail, 'Just John', 'avatar_of_john')

        when:
        def editUserResponse = patch("/admin/users/${userEmail}", [
                name: invalidName
        ], adminToken)

        then:
        editUserResponse.code() == 400

        and:
        def usersResponse = get("/admin/users/${userEmail}", adminToken)
        usersResponse.code() == 200
        usersResponse.body().name == 'Just John'

        where:
        invalidName << ['', '   ', null]
    }

    @Unroll
    def "should edit an User's avatar (new avatar: #updatedAvatar)"(String updatedAvatar) {
        given:
        def userEmail = 'john@user.com'
        createUser(userEmail, 'Just John', 'avatar_of_john')

        when:
        def editUserResponse = patch("/admin/users/${userEmail}", [
                avatarHref: updatedAvatar
        ], adminToken)

        then:
        editUserResponse.code() == 204

        and:
        def usersResponse = get("/admin/users/${userEmail}", adminToken)
        usersResponse.code() == 200
        usersResponse.body().name == 'Just John'
        usersResponse.body().avatarHref == updatedAvatar

        where:
        updatedAvatar << ['UPDATED_avatar_of_john', '', null]
    }

    def "should fail to edit an User which does not exist"() {
        when:
        def editUserResponse = patch('/admin/users/no@user.com', [
                name: 'John Updated'
        ], adminToken)

        then:
        editUserResponse.code() == 404
        editUserResponse.body().code == 2001
        editUserResponse.body().message == "User 'no@user.com' does not exist"
    }

    @Unroll
    def "should not edit an User if has no privilege to do so (permission: #nonAdminPermission)"(PermissionType nonAdminPermission) {
        given:
        createUser('non-admin@user.com')
        assignGlobalPermissions('non-admin@user.com', [nonAdminPermission])
        def nonAdminToken = createAccessTokenFor('non-admin@user.com')

        and:
        def userEmail = 'john@user.com'
        createUser(userEmail, 'Just John', 'avatar_of_john')

        when:
        def editUserResponse = patch("/admin/users/${userEmail}", [
                name: 'John Updated'
        ], nonAdminToken)

        then:
        editUserResponse.code() == 403

        where:
        nonAdminPermission << PermissionType.values() - PermissionType.ADMIN
    }

    def "User should be able to edit its data even if has no permission at all"() {
        given:
        def userEmail = 'any.user@user.com'
        createUser(userEmail, 'Just John', 'avatar_of_john')
        def userToken = createAccessTokenFor(userEmail)

        when:
        def editUserResponse = patch("/admin/users/${userEmail}", [
                name      : 'John UPDATED',
                avatarHref: 'UPDATED_avatar_of_john'
        ], userToken)

        then:
        editUserResponse.code() == 204

        and:
        def usersResponse = get("/admin/users/${userEmail}", adminToken)
        usersResponse.code() == 200
        usersResponse.body().name == 'John UPDATED'
        usersResponse.body().avatarHref == 'UPDATED_avatar_of_john'
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

    private assignGlobalPermissions(String userEmail, List<PermissionType> permissions) {
        setupHelper.assignGlobalPermissions(userEmail, permissions)
    }

}
