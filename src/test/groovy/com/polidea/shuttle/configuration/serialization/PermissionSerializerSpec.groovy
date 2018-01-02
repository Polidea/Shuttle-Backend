package com.polidea.shuttle.configuration.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.polidea.shuttle.domain.user.permissions.PermissionType
import com.polidea.shuttle.infrastructure.json.PermissionTypeSerializer
import spock.lang.Specification
import spock.lang.Unroll

class PermissionSerializerSpec extends Specification {

    JsonGenerator gen = Mock(JsonGenerator)

    PermissionTypeSerializer serializer = new PermissionTypeSerializer()

    @Unroll
    def "should serialize #permission permission"() {
        when:
        serializer.serialize(permission, gen, null)

        then:
        1 * gen.writeString(expectedPermissionName)

        where:
        permission                   | expectedPermissionName
        PermissionType.ADMIN         | 'admin_access'
        PermissionType.PUBLISHER     | 'can_publish'
        PermissionType.BUILD_CREATOR | 'can_create_build'
    }

}
