package com.polidea.shuttle.web.rest.authorization

import com.polidea.shuttle.domain.build.BuildRepository
import com.polidea.shuttle.web.rest.MockMvcIntegrationSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.annotation.Transactional
import spock.lang.Unroll

import static com.polidea.shuttle.TestConstants.ANDROID_PLATFORM_NAME
import static com.polidea.shuttle.TestConstants.MANY_USERS_ACCESS_PERMISSIONS_SCRIPT_PATH
import static com.polidea.shuttle.TestConstants.TEST_APP_ID
import static com.polidea.shuttle.TestConstants.TEST_PROJECT_ID
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_ADMIN
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_MODERATOR
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_NORMAL_USER
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_PUBLISHER
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_USER_ASSIGNED_APP
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_VIEWER
import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Transactional
@Sql(MANY_USERS_ACCESS_PERMISSIONS_SCRIPT_PATH)
class AdminBuildControllerAuthorizationSpec extends MockMvcIntegrationSpecification {

    @Autowired
    BuildRepository buildRepository

    @Unroll
    def "should return status code #statusCode.name() for token #tokenValue for get builds for admin panel"(String tokenValue, HttpStatus statusCode) {
        given:
        def endpoint = "/admin/projects/$TEST_PROJECT_ID/apps/$ANDROID_PLATFORM_NAME/$TEST_APP_ID/builds"

        when:
        def result = get(endpoint, tokenValue)

        then:
        result.andExpect(status().is(statusCode.value()))

        where:
        tokenValue                   | statusCode
        TEST_TOKEN_ADMIN             | OK
        TEST_TOKEN_MODERATOR         | OK
        TEST_TOKEN_PUBLISHER         | OK
        TEST_TOKEN_VIEWER            | OK
        TEST_TOKEN_USER_ASSIGNED_APP | OK
        TEST_TOKEN_NORMAL_USER       | FORBIDDEN
    }

    @Unroll
    def "should return status code #statusCode.name() for token #tokenValue for delete build"(String tokenValue, HttpStatus statusCode) {
        given:
        def endpoint = "/admin/projects/$TEST_PROJECT_ID/apps/$ANDROID_PLATFORM_NAME/$TEST_APP_ID/builds/123"

        when:
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
