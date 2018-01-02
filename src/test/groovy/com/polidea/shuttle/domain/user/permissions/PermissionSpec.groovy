package com.polidea.shuttle.domain.user.permissions

import spock.lang.Specification
import spock.lang.Unroll

class PermissionSpec extends Specification {

    static final NAMES = ['can_publish', 'admin_access', 'unknown_name']

    def "should thrown InvalidPermissionTypeException when cannot determine by name"() {
        when:
        PermissionType.determinePermissionType('unknown_permission_name')

        then:
        InvalidPermissionTypeException thrownException = thrown()
        thrownException.getMessage() == "Permission 'unknown_permission_name' is invalid"
    }

    def "should throw NullPointerException when null is given"() {
        when:
        PermissionType.determinePermissionType(null)

        then:
        NullPointerException thrownException = thrown()
        thrownException.getMessage() == "'permission' must not be null"
    }

    @Unroll
    def "should determine #expectedPermission permission name properly"() {
        when:
        PermissionType permission = PermissionType.determinePermissionType(permissionName)

        then:
        permission == expectedPermission

        where:
        permissionName     | expectedPermission
        'can_publish'      | PermissionType.PUBLISHER
        'admin_access'     | PermissionType.ADMIN
        'can_create_build' | PermissionType.BUILD_CREATOR
    }

}
