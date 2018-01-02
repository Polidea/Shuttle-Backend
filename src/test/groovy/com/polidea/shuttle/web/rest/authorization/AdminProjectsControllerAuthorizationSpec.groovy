package com.polidea.shuttle.web.rest.authorization

import com.polidea.shuttle.web.rest.MockMvcIntegrationSpecification
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import spock.lang.Unroll

import static com.polidea.shuttle.TestConstants.MANY_USERS_ACCESS_PERMISSIONS_SCRIPT_PATH
import static com.polidea.shuttle.TestConstants.TEST_PROJECT_ID
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_ADMIN
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_MODERATOR
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_NORMAL_USER
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_PUBLISHER
import static groovy.json.JsonOutput.toJson
import static org.springframework.http.HttpStatus.CONFLICT
import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Sql(MANY_USERS_ACCESS_PERMISSIONS_SCRIPT_PATH)
class AdminProjectsControllerAuthorizationSpec extends MockMvcIntegrationSpecification {

    static final String ENDPOINT = "/admin/projects"

    static final VALID_REQUEST = toJson([name: 'Project 1', iconHref: "http://a.com/a.png"])

    @Unroll
    def "should return status code #statusCode.name() for token #tokenValue when list all projects"(String tokenValue, HttpStatus statusCode) {
        when:
        def result = get(ENDPOINT, tokenValue)

        then:
        result.andExpect(status().is(statusCode.value()))

        where:
        tokenValue             | statusCode
        TEST_TOKEN_ADMIN       | OK
        TEST_TOKEN_MODERATOR   | OK
        TEST_TOKEN_PUBLISHER   | OK
        TEST_TOKEN_NORMAL_USER | OK
    }

    @Unroll
    def "should return status code #statusCode.name() for token #tokenValue when add project"(String tokenValue, HttpStatus statusCode) {
        when:
        def result = post(ENDPOINT, VALID_REQUEST, tokenValue)

        then:
        result.andExpect(status().is(statusCode.value()))

        where:
        tokenValue             | statusCode
        TEST_TOKEN_ADMIN       | CONFLICT
        TEST_TOKEN_MODERATOR   | FORBIDDEN
        TEST_TOKEN_PUBLISHER   | FORBIDDEN
        TEST_TOKEN_NORMAL_USER | FORBIDDEN
    }

    @Unroll
    def "should return status code #statusCode.name() for token #tokenValue when edit project"(String tokenValue, HttpStatus statusCode) {
        when:
        def result = patch("$ENDPOINT/$TEST_PROJECT_ID", VALID_REQUEST, tokenValue)

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
    def "should return status code #statusCode.name() for token #tokenValue when delete project"(String tokenValue, HttpStatus statusCode) {
        when:
        def result = delete("$ENDPOINT/$TEST_PROJECT_ID", tokenValue)

        then:
        result.andExpect(status().is(statusCode.value()))

        where:
        tokenValue             | statusCode
        TEST_TOKEN_ADMIN       | NO_CONTENT
        TEST_TOKEN_MODERATOR   | FORBIDDEN
        TEST_TOKEN_PUBLISHER   | FORBIDDEN
        TEST_TOKEN_NORMAL_USER | FORBIDDEN
    }
}

