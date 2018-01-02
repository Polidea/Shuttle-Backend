package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.user.permissions.PermissionType

import static com.polidea.shuttle.test_config.DefaultAvatarsConfigurationForTests.FIRST_DEFAULT_AVATAR_URL
import static com.polidea.shuttle.test_config.DefaultAvatarsConfigurationForTests.NEXT_RANDOM_DEFAULT_AVATAR_URL
import static com.polidea.shuttle.test_config.DefaultAvatarsConfigurationForTests.SECOND_DEFAULT_AVATAR_URL

class ClientAvatarsControllerSpec extends HttpIntegrationSpecification {

    def "get list of default avatars"() {
        given:
        createUser('regular.user@shuttle.com')
        def userToken = createClientTokenFor('regular.user@shuttle.com')

        when:
        def avatarsResponse = get("/avatars", userToken)

        then:
        avatarsResponse.code() == 200
        avatarsResponse.body().avatars*.imageHref == [
                FIRST_DEFAULT_AVATAR_URL,
                SECOND_DEFAULT_AVATAR_URL
        ]
    }

    def "new User has by default assigned one of default avatars"() {
        given:
        createUser('admin@shuttle.com')
        assignAdminPermissionTo('admin@shuttle.com')
        def adminToken = createClientTokenFor('admin@shuttle.com')

        when:
        def createUserResponse = post("/admin/users", [
                email: 'john@shuttle.com',
                name : 'John'
        ], adminToken)
        assert createUserResponse.code() == 204

        then:
        def getUserResponse = get("/admin/users/john@shuttle.com", adminToken)
        getUserResponse.code() == 200
        getUserResponse.body().avatarHref == NEXT_RANDOM_DEFAULT_AVATAR_URL
    }

    private void createUser(String userEmail) {
        setupHelper.createUser(userEmail, 'Any User', 'any.user.avatar.href')
    }

    private void assignAdminPermissionTo(String userEmail) {
        setupHelper.assignGlobalPermissions(userEmail, [PermissionType.ADMIN])
    }

    private String createClientTokenFor(String userEmail) {
        return setupHelper.createClientAccessToken(userEmail, 'any-device-id')
    }

}
