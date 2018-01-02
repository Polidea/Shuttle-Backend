package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.user.permissions.PermissionType
import com.polidea.shuttle.domain.user.permissions.global.GlobalPermissionsService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

class AdminGlobalPermissionsControllerSpec extends HttpIntegrationSpecification {

    @Autowired
    GlobalPermissionsService globalPermissionsService

    String adminToken

    void setup() {
        createUser('admin@user.com')
        assignGlobalPermissions('admin@user.com', [PermissionType.ADMIN])
        adminToken = createAccessTokenFor('admin@user.com')
    }

    def 'User should have only "CAN_MUTE" permission by default'() {
        given:
        def userEmail = 'john@user.com'
        createUser(userEmail)

        expect:
        def userResponse = get("/admin/users/${userEmail}", adminToken)
        userResponse.code() == 200
        userResponse.body().globalPermissions == ["can_mute"]
    }

    def "should find all global admins"() {
        when:
        createUser('john@user.com')
        assignGlobalPermissions('john@user.com', [PermissionType.ADMIN])

        then:
        globalPermissionsService.findAllGlobalAdministrators().size() == 2
    }

    def "should find only global admins who were not deleted"() {
        when:
        createUser('john@user.com')
        assignGlobalPermissions('john@user.com', [PermissionType.ADMIN])
        deleteUser('john@user.com')

        then:
        globalPermissionsService.findAllGlobalAdministrators().size() == 1
    }

    def "should set Global Permissions"() {
        given:
        def userEmail = 'john@user.com'
        createUser(userEmail)

        when:
        def setGlobalPermissionsResponse = post("/admin/users/${userEmail}/permissions", [permissions: [
                'can_archive', 'can_publish'
        ]], adminToken)

        then:
        setGlobalPermissionsResponse.code() == 204

        and:
        def userResponse = get("/admin/users/${userEmail}", adminToken)
        userResponse.code() == 200
        userResponse.body().globalPermissions.toSet() == ['can_archive', 'can_publish'].toSet()
    }

    def "should override existing Global Permissions with new ones"() {
        given:
        def userEmail = 'john@user.com'
        createUser(userEmail)

        and:
        assignGlobalPermissions(userEmail, [PermissionType.ADMIN, PermissionType.ARCHIVER])

        when:
        def setGlobalPermissionsResponse = post("/admin/users/${userEmail}/permissions", [permissions: [
                'can_archive', 'can_publish'
        ]], adminToken)

        then:
        setGlobalPermissionsResponse.code() == 204

        and:
        def userResponse = get("/admin/users/${userEmail}", adminToken)
        userResponse.code() == 200
        userResponse.body().globalPermissions as Set == ['can_archive', 'can_publish'] as Set
    }

    def "should remove existing Global Permissions"() {
        given:
        def userEmail = 'john@user.com'
        createUser(userEmail)

        and:
        def setGlobalPermissionsResponse = post("/admin/users/${userEmail}/permissions", [permissions: [
                'can_archive', 'can_publish'
        ]], adminToken)
        assert setGlobalPermissionsResponse.code() == 204

        when:
        def removeGlobalPermissionsResponse = post("/admin/users/${userEmail}/permissions", [permissions: []], adminToken)

        then:
        removeGlobalPermissionsResponse.code() == 204

        and:
        def userResponse = get("/admin/users/${userEmail}", adminToken)
        userResponse.code() == 200
        userResponse.body().globalPermissions == []
    }

    @Unroll
    def "should fail to change Global Permissions if if has no privilege to do so (permission: #nonAdminPermission)"(PermissionType nonAdminPermission) {
        given:
        def userEmail = 'john@user.com'
        createUser(userEmail)

        and:
        createUser('non-admin@user.com')
        assignGlobalPermissions('non-admin@user.com', [nonAdminPermission])
        def nonAdminToken = createAccessTokenFor('non-admin@user.com')

        when:
        def setGlobalPermissionsResponse = post("/admin/users/${userEmail}/permissions", [permissions: [
                'can_archive', 'can_publish'
        ]], nonAdminToken)

        then:
        setGlobalPermissionsResponse.code() == 403

        where:
        nonAdminPermission << PermissionType.values() - PermissionType.ADMIN
    }

    private void createUser(String userEmail) {
        setupHelper.createUser(userEmail, "Name of ${userEmail}", null)
    }

    private void deleteUser(String userEmail) {
        setupHelper.deleteUser(userEmail);
    }

    private String createAccessTokenFor(String userEmail) {
        return setupHelper.createClientAccessToken(userEmail, 'any-device-id')
    }

    private void assignGlobalPermissions(String userEmail, List<PermissionType> permissions) {
        setupHelper.assignGlobalPermissions(userEmail, permissions)
    }

}
