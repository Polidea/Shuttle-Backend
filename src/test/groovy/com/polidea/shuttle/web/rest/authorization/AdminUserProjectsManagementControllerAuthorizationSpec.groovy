package com.polidea.shuttle.web.rest.authorization

import com.polidea.shuttle.web.rest.MockMvcIntegrationSpecification
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import spock.lang.Unroll

import static com.polidea.shuttle.TestConstants.MANY_USERS_ACCESS_PERMISSIONS_SCRIPT_PATH
import static com.polidea.shuttle.TestConstants.TEST_EMAIL_NORMAL_USER
import static com.polidea.shuttle.TestConstants.TEST_PROJECT_ID
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_ADMIN
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_MODERATOR
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_NORMAL_USER
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_PUBLISHER
import static groovy.json.JsonOutput.toJson
import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Sql(MANY_USERS_ACCESS_PERMISSIONS_SCRIPT_PATH)
class AdminUserProjectsManagementControllerAuthorizationSpec extends MockMvcIntegrationSpecification {

    static final String VALID_REQUEST = toJson([])

    private static final String ENDPOINT = '/admin/projects'

    @Unroll
    def "should return status code #statusCode.name() for token #tokenValue when assigned app to user"(String tokenValue, HttpStatus statusCode) {
        when:
        def endpoint = "$ENDPOINT/$TEST_PROJECT_ID/users/$TEST_EMAIL_NORMAL_USER"
        def result = post(endpoint, VALID_REQUEST, tokenValue)

        then:
        result.andExpect(status().is(statusCode.value()))

        where:
        tokenValue             | statusCode
        TEST_TOKEN_ADMIN       | NO_CONTENT
        TEST_TOKEN_MODERATOR   | NO_CONTENT
        TEST_TOKEN_PUBLISHER   | FORBIDDEN
        TEST_TOKEN_NORMAL_USER | FORBIDDEN
    }

    @Unroll
    def "should return status code #statusCode.name() for token #tokenValue when unassigned app from user"(String tokenValue, HttpStatus statusCode) {
        when:
        def endpoint = "$ENDPOINT/$TEST_PROJECT_ID/users/$TEST_EMAIL_NORMAL_USER"
        def result = delete(endpoint, tokenValue)

        then:
        result.andExpect(status().is(statusCode.value()))

        where:
        tokenValue             | statusCode
        TEST_TOKEN_ADMIN       | NO_CONTENT
        TEST_TOKEN_MODERATOR   | NO_CONTENT
        TEST_TOKEN_PUBLISHER   | FORBIDDEN
        TEST_TOKEN_NORMAL_USER | FORBIDDEN
    }
}

