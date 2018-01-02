package com.polidea.shuttle.web.rest.authorization

import com.polidea.shuttle.web.rest.MockMvcIntegrationSpecification
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import spock.lang.Unroll

import static com.polidea.shuttle.TestConstants.MANY_USERS_ACCESS_PERMISSIONS_SCRIPT_PATH
import static com.polidea.shuttle.TestConstants.TEST_EMAIL
import static com.polidea.shuttle.TestConstants.TEST_PROJECT_ID
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_ADMIN
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_MODERATOR
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_NORMAL_USER
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_PUBLISHER
import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Sql(MANY_USERS_ACCESS_PERMISSIONS_SCRIPT_PATH)
class AdminProjectPermissionsControllerAuthorizationSpec extends MockMvcIntegrationSpecification {

    static final String ENDPOINT = '/admin/projects'

    @Unroll
    def "should return status code #statusCode.name() for token #tokenValue when assigned publisher permissions per app for user"(String tokenValue, HttpStatus statusCode) {
        given:
        def endpoint = "$ENDPOINT/$TEST_PROJECT_ID/users/$TEST_EMAIL/permission/CAN_MUTE/"

        when:
        def result = post(endpoint, tokenValue)

        then:
        result.andExpect(status().is(statusCode.value()))

        where:
        tokenValue             | statusCode
        TEST_TOKEN_ADMIN       | NOT_FOUND
        TEST_TOKEN_MODERATOR   | NOT_FOUND
        TEST_TOKEN_PUBLISHER   | FORBIDDEN
        TEST_TOKEN_NORMAL_USER | FORBIDDEN
    }

    @Unroll
    def "should return status code #statusCode.name() for token #token when assigned admin permissions per project for user"(String token, HttpStatus statusCode) {
        given:
        def endpoint = "$ENDPOINT/$TEST_PROJECT_ID/users/$TEST_EMAIL/permission/ADMIN_ACCESS/"

        when:
        def result = post(endpoint, token)

        then:
        result.andExpect(status().is(statusCode.value()))

        where:
        token                  | statusCode
        TEST_TOKEN_ADMIN       | NOT_FOUND
        TEST_TOKEN_MODERATOR   | NOT_FOUND
        TEST_TOKEN_PUBLISHER   | FORBIDDEN
        TEST_TOKEN_NORMAL_USER | FORBIDDEN
    }

}

