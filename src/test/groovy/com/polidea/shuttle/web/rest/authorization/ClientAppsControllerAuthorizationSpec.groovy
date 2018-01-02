package com.polidea.shuttle.web.rest.authorization

import com.polidea.shuttle.web.rest.MockMvcIntegrationSpecification
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import spock.lang.Unroll

import static com.polidea.shuttle.TestConstants.ANDROID_PLATFORM_NAME
import static com.polidea.shuttle.TestConstants.MANY_USERS_ACCESS_PERMISSIONS_SCRIPT_PATH
import static com.polidea.shuttle.TestConstants.TEST_APP_ID
import static com.polidea.shuttle.TestConstants.TEST_PROJECT_ID
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_ADMIN
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_MODERATOR
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_MUTER
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_NORMAL_USER
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_PUBLISHER
import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Sql(MANY_USERS_ACCESS_PERMISSIONS_SCRIPT_PATH)
class ClientAppsControllerAuthorizationSpec extends MockMvcIntegrationSpecification {

    @Unroll
    def "should return status code #statusCode.name() for token #tokenValue when mute app"(String tokenValue, HttpStatus statusCode) {
        when:
        def result = post("/projects/$TEST_PROJECT_ID/apps/$ANDROID_PLATFORM_NAME/$TEST_APP_ID/mute", tokenValue)

        then:
        result.andExpect(status().is(statusCode.value()))

        where:
        tokenValue             | statusCode
        TEST_TOKEN_ADMIN       | FORBIDDEN
        TEST_TOKEN_MODERATOR   | FORBIDDEN
        TEST_TOKEN_PUBLISHER   | FORBIDDEN
        TEST_TOKEN_NORMAL_USER | FORBIDDEN
        TEST_TOKEN_MUTER       | NO_CONTENT
    }

    @Unroll
    def "should return status code #statusCode.name() for token #tokenValue when unmute project"(String tokenValue, HttpStatus statusCode) {
        when:
        def result = delete("/projects/$TEST_PROJECT_ID/apps/$ANDROID_PLATFORM_NAME/$TEST_APP_ID/mute", tokenValue)

        then:
        result.andExpect(status().is(statusCode.value()))

        where:
        tokenValue             | statusCode
        TEST_TOKEN_ADMIN       | FORBIDDEN
        TEST_TOKEN_MODERATOR   | FORBIDDEN
        TEST_TOKEN_PUBLISHER   | FORBIDDEN
        TEST_TOKEN_NORMAL_USER | FORBIDDEN
        TEST_TOKEN_MUTER       | NO_CONTENT
    }
}



