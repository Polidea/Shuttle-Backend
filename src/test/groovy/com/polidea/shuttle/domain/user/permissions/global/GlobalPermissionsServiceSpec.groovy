package com.polidea.shuttle.domain.user.permissions.global

import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.UserNotFoundException
import com.polidea.shuttle.domain.user.UserRepository
import com.polidea.shuttle.domain.user.permissions.PermissionType
import com.polidea.shuttle.domain.user.permissions.global.input.PermissionsAssignmentRequest
import spock.lang.Specification

import static com.polidea.shuttle.TestConstants.TEST_EMAIL
import static com.polidea.shuttle.domain.user.permissions.PermissionType.ADMIN
import static com.polidea.shuttle.domain.user.permissions.PermissionType.MUTER
import static com.polidea.shuttle.domain.user.permissions.PermissionType.PUBLISHER

class GlobalPermissionsServiceSpec extends Specification {

    GlobalPermissionsService globalPermissionService

    GlobalPermissionRepository globalPermissionRepositoryMock = Mock(GlobalPermissionRepository)

    UserRepository userRepositoryMock = Mock(UserRepository)

    void setup() {
        globalPermissionService = new GlobalPermissionsService(globalPermissionRepositoryMock, userRepositoryMock)
    }

    def 'should throw exception when user is not found'() {
        given:
        userRepositoryMock.findUser(TEST_EMAIL) >> Optional.empty()

        when:
        globalPermissionService.assignPermissions(
                new PermissionsAssignmentRequest(permissions: [PUBLISHER]),
                TEST_EMAIL
        )

        then:
        thrown(UserNotFoundException)
    }

    def 'should assign completely new permissions to user'() {
        given:
        def permissions = [ADMIN, MUTER]
        def user = new User()

        when:
        globalPermissionService.assignPermissions(user, permissions)

        then:
        1 * globalPermissionRepositoryMock.delete(user)

        then:
        1 * globalPermissionRepositoryMock.createGlobalPermissions(user, permissions)
    }
}
