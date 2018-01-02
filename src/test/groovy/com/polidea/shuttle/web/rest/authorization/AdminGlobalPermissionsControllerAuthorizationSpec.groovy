package com.polidea.shuttle.web.rest.authorization

import com.polidea.shuttle.web.rest.MockMvcIntegrationSpecification
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import spock.lang.Unroll

import static com.polidea.shuttle.TestConstants.MANY_USERS_ACCESS_PERMISSIONS_SCRIPT_PATH
import static com.polidea.shuttle.TestConstants.PUBLISHER_PERMISSION_NAME
import static com.polidea.shuttle.TestConstants.TEST_EMAIL
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_ADMIN
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_MODERATOR
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_NORMAL_USER
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_PUBLISHER
import static groovy.json.JsonOutput.toJson
import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Sql(MANY_USERS_ACCESS_PERMISSIONS_SCRIPT_PATH)
class AdminGlobalPermissionsControllerAuthorizationSpec extends MockMvcIntegrationSpecification {

    static final String ENDPOINT = '/admin/users'

    static final String VALID_REQUEST = toJson([permissions: [PUBLISHER_PERMISSION_NAME]])

    @Unroll
    def "should return status code #statusCode.name() for token #tokenValue when permissions are assigned"(String tokenValue, HttpStatus statusCode) {
        given:
        def endpoint = "$ENDPOINT/$TEST_EMAIL/permissions"

        when:
        def result = post(endpoint, VALID_REQUEST, tokenValue)

        then:
        result.andExpect(status().is(statusCode.value()))

        where:
        tokenValue             | statusCode
        TEST_TOKEN_ADMIN       | NOT_FOUND
        TEST_TOKEN_MODERATOR   | FORBIDDEN
        TEST_TOKEN_PUBLISHER   | FORBIDDEN
        TEST_TOKEN_NORMAL_USER | FORBIDDEN
    }

}

