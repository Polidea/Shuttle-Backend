package com.polidea.shuttle.web.rest.authorization

import com.polidea.shuttle.domain.user.UserRepository
import com.polidea.shuttle.domain.user.permissions.PermissionType
import com.polidea.shuttle.domain.user.permissions.global.GlobalPermissionsService
import com.polidea.shuttle.domain.user.permissions.global.input.PermissionsAssignmentRequest
import com.polidea.shuttle.domain.user.access_token.AccessTokenRepository
import com.polidea.shuttle.domain.user.access_token.TokenType
import com.polidea.shuttle.web.rest.MockMvcIntegrationSpecification
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

import java.time.Duration
import java.time.Instant

import static com.polidea.shuttle.test_config.TimeServiceConfigurationForTests.CURRENT_TIME_IN_TESTS
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.notNullValue
import static org.springframework.http.HttpStatus.UNAUTHORIZED
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class TokenExpirationSpec extends MockMvcIntegrationSpecification {

    private static final String ENDPOINT_WHICH_REQUIRES_CLIENT_TOKEN = '/users'

    @Autowired
    UserRepository userRepository
    @Autowired
    GlobalPermissionsService globalPermissionsService
    @Autowired
    AccessTokenRepository tokenRepository

    String tokenOwnerEmail = 'token@owner'

    @Override
    void setup() {
        userRepository.createUser(tokenOwnerEmail, 'Token Owner', null, false)
        globalPermissionsService.assignPermissions(new PermissionsAssignmentRequest(permissions: [PermissionType.ADMIN]), tokenOwnerEmail)
    }

    @Unroll
    def "should authorize with token #tokenValue which has not expired yet"() {
        given:
        def token = saveNewToken(tokenValue, creationTimestamp)

        when:
        def result = get(ENDPOINT_WHICH_REQUIRES_CLIENT_TOKEN, token)

        then:
        result.andExpect(status().isOk())

        where:
        tokenValue           | creationTimestamp
        'justCreatedToken'   | CURRENT_TIME_IN_TESTS
        'nearlyExpiredToken' | CURRENT_TIME_IN_TESTS - Duration.ofDays(30)
    }

    def "should not authorize with token which has expired"() {
        given:
        def token = saveNewToken(
                'expiredToken',
                CURRENT_TIME_IN_TESTS - Duration.ofDays(90) - Duration.ofMillis(1)
        )

        when:
        def result = get(ENDPOINT_WHICH_REQUIRES_CLIENT_TOKEN, token)

        then:
        result.andExpect(status().isUnauthorized())
              .andExpect(jsonPath('$.timestamp', notNullValue()))
              .andExpect(jsonPath('$.status', equalTo(UNAUTHORIZED.value())))
              .andExpect(jsonPath('$.error', equalTo(UNAUTHORIZED.getReasonPhrase())))
              .andExpect(jsonPath('$.message', equalTo('Access Token is invalid')))
    }

    def "should not authorize with token which was created in a future"() {
        given:
        def token = saveNewToken(
                'futureToken',
                CURRENT_TIME_IN_TESTS + Duration.ofMillis(1)
        )

        when:
        def result = get(ENDPOINT_WHICH_REQUIRES_CLIENT_TOKEN, token)

        then:
        result.andExpect(status().isUnauthorized())
              .andExpect(jsonPath('$.timestamp', notNullValue()))
              .andExpect(jsonPath('$.status', equalTo(UNAUTHORIZED.value())))
              .andExpect(jsonPath('$.error', equalTo(UNAUTHORIZED.getReasonPhrase())))
              .andExpect(jsonPath('$.message', equalTo('Access Token is invalid')))
    }

    private String saveNewToken(String tokenValue, Instant creationTimestamp) {
        def user = userRepository.findUser(tokenOwnerEmail).get()
        tokenRepository.createAccessToken(
                user,
                'any-device-id',
                TokenType.CLIENT,
                tokenValue,
                creationTimestamp
        )
        return tokenValue
    }

}
