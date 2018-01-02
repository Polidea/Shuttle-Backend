package com.polidea.shuttle.web.rest.authorization

import com.polidea.shuttle.web.rest.MockMvcIntegrationSpecification
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.ResultActions
import spock.lang.Unroll

import static com.polidea.shuttle.TestConstants.MANY_USERS_ACCESS_PERMISSIONS_SCRIPT_PATH
import static com.polidea.shuttle.TestConstants.TEST_EMAIL
import static com.polidea.shuttle.TestConstants.TEST_PROJECT_ID
import static com.polidea.shuttle.TestConstants.TEST_SOME_USER_EMAIL
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_ADMIN
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_MODERATOR
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_NORMAL_USER
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_OTHER_MODERATOR
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_PUBLISHER
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_SOME_USER
import static groovy.json.JsonOutput.toJson
import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Sql(MANY_USERS_ACCESS_PERMISSIONS_SCRIPT_PATH)
class AdminUserControllerAuthorizationSpec extends MockMvcIntegrationSpecification {

    private final static NEW_USER_JSON = toJson([
            email: 'any@user.com',
            name : 'Any User'
    ])

    @Unroll
    def "should return status code #statusCode.name() for token #tokenValue when new user is added"(String tokenValue, HttpStatus statusCode) {
        when:
        ResultActions result = post('/admin/users', NEW_USER_JSON, tokenValue)

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
    def "should return status code #statusCode.name() for token #tokenValue when try to list all users"(String tokenValue, HttpStatus statusCode) {
        when:
        ResultActions result = get('/admin/users', tokenValue)

        then:
        result.andExpect(status().is(statusCode.value()))

        where:
        tokenValue             | statusCode
        TEST_TOKEN_ADMIN       | OK
        TEST_TOKEN_MODERATOR   | OK
        TEST_TOKEN_PUBLISHER   | FORBIDDEN
        TEST_TOKEN_NORMAL_USER | FORBIDDEN
    }

    @Unroll
    def "should return status code #statusCode.name() for token #tokenValue when try to delete user"(String tokenValue, HttpStatus statusCode) {
        when:
        ResultActions result = delete("/admin/users/$TEST_EMAIL", tokenValue)

        then:
        result.andExpect(status().is(statusCode.value()))

        where:
        tokenValue             | statusCode
        TEST_TOKEN_ADMIN       | NOT_FOUND
        TEST_TOKEN_MODERATOR   | FORBIDDEN
        TEST_TOKEN_PUBLISHER   | FORBIDDEN
        TEST_TOKEN_NORMAL_USER | FORBIDDEN
    }

    @Unroll
    def "should return status code #statusCode.name() for token #tokenValue when listing users of some project"(String tokenValue, HttpStatus statusCode) {
        when:
        ResultActions result = get("/admin/projects/$TEST_PROJECT_ID/users", tokenValue)

        then:
        result.andExpect(status().is(statusCode.value()))

        where:
        tokenValue                     | statusCode
        TEST_TOKEN_ADMIN               | OK
        TEST_TOKEN_MODERATOR           | OK
        TEST_TOKEN_OTHER_MODERATOR     | FORBIDDEN
        TEST_TOKEN_PUBLISHER           | FORBIDDEN
        TEST_TOKEN_NORMAL_USER         | FORBIDDEN
    }

    @Unroll
    def "should return status code #statusCode.name() for token #tokenValue when try to get user profile"(String tokenValue, HttpStatus statusCode) {
        when:
        ResultActions result = get("/admin/users/$TEST_SOME_USER_EMAIL", tokenValue)

        then:
        result.andExpect(status().is(statusCode.value()))

        where:
        tokenValue             | statusCode
        TEST_TOKEN_ADMIN       | OK
        TEST_TOKEN_MODERATOR   | OK
        TEST_TOKEN_SOME_USER   | OK
        TEST_TOKEN_PUBLISHER   | FORBIDDEN
        TEST_TOKEN_NORMAL_USER | FORBIDDEN
    }

}

