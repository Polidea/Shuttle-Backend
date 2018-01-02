package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.user.permissions.PermissionType
import spock.lang.Unroll

class AdministrationEndpointsAccess_HttpSpec extends HttpIntegrationSpecification {

    def "should access health endpoint without authentication"() {
        when:
        def response = getWithoutAccessToken('/health')

        then:
        response.code() == 200
    }

    @Unroll
    def "should access administration endpoint #path as global admin"(String path) {
        given:
        createUser('global-admin@shuttle.com')
        assignGlobalPermissions('global-admin@shuttle.com', [PermissionType.ADMIN])
        String token = createAccessTokenFor('global-admin@shuttle.com')

        when:
        def response = get(path, token)

        then:
        response.code() == 200

        where:
        path << [
                '/autoconfig',
                '/beans',
                '/configprops',
                '/env',
                '/flyway',
                '/mappings',
                '/metrics'
        ]
    }

    @Unroll
    def "should not access administration endpoint #path as not global admin"(String path) {
        given:
        createUser('non-global-admin@shuttle.com')
        assignGlobalPermissions('non-global-admin@shuttle.com', (PermissionType.values() - PermissionType.ADMIN).toList())
        String token = createAccessTokenFor('non-global-admin@shuttle.com')

        when:
        def response = get(path, token)

        then:
        response.code() == 403

        where:
        path << [
                '/autoconfig',
                '/beans',
                '/configprops',
                '/env',
                '/flyway',
                '/mappings',
                '/metrics'
        ]
    }

    private void createUser(String userEmail) {
        setupHelper.createUser(userEmail, "Name of ${userEmail}", null)
    }

    private void assignGlobalPermissions(String userEmail, List<PermissionType> permissions) {
        setupHelper.assignGlobalPermissions(userEmail, permissions)
    }

    private String createAccessTokenFor(String userEmail) {
        return setupHelper.createClientAccessToken(userEmail, 'any-device-id')
    }

}
