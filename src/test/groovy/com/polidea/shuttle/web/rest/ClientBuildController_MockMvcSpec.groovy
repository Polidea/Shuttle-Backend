package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.build.BuildRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.ResultActions
import org.springframework.transaction.annotation.Transactional

import static com.polidea.shuttle.TestConstants.ANDROID_PLATFORM_NAME
import static com.polidea.shuttle.TestConstants.CLIENT_BUILD_CONTROLLER_DATA_PATH
import static com.polidea.shuttle.TestConstants.TEST_APP_ID
import static com.polidea.shuttle.TestConstants.TEST_PROJECT_ID
import static com.polidea.shuttle.TestConstants.TEST_TOKEN
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_USER
import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.isEmptyString
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.notNullValue
import static org.hamcrest.Matchers.nullValue
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Transactional
@Sql(CLIENT_BUILD_CONTROLLER_DATA_PATH)
class ClientBuildController_MockMvcSpec extends MockMvcIntegrationSpecification {

    @Autowired
    BuildRepository buildRepository

    def "should return 400 error when trying to list builds for bad platform name"() {
        given:
        def endpoint = "/projects/$TEST_PROJECT_ID/apps/badplatformname/$TEST_APP_ID/builds"

        when:
        def result = get(endpoint, TEST_TOKEN)

        then:
        result.andExpect(status().isBadRequest())
    }

    def "should get all android app builds for publisher"() {
        given:
        def endpoint = "/projects/$TEST_PROJECT_ID/apps/$ANDROID_PLATFORM_NAME/$TEST_APP_ID/builds"

        when:
        def result = get(endpoint, TEST_TOKEN)

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.builds', hasSize(2)))
              .andExpect(jsonPath('$.builds[0].versionCode', not(isEmptyString())))
        assertCommonFieldsCorrectness(result)
    }

    def "should get all ios builds for publisher"() {
        given:
        def endpoint = "/projects/$TEST_PROJECT_ID/apps/ios/$TEST_APP_ID/builds"

        when:
        def result = get(endpoint, TEST_TOKEN)

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.builds', hasSize(1)))
              .andExpect(jsonPath('$.builds[0].prefixSchema', not(isEmptyString())))
        assertCommonFieldsCorrectness(result)
    }

    def "should get published builds for app which is assigned to user"() {
        given:
        def endpoint = "/projects/$TEST_PROJECT_ID/apps/$ANDROID_PLATFORM_NAME/$TEST_APP_ID/builds"

        when:
        def result = get(endpoint, TEST_TOKEN_USER)

        then:
        // There is one published build for android app
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.builds', hasSize(1)))
        assertCommonFieldsCorrectness(result)
    }

    def "should get builds for Publisher and canPublish flag is set to true"() {
        given:
        def endpoint = "/projects/$TEST_PROJECT_ID/apps/$ANDROID_PLATFORM_NAME/$TEST_APP_ID/builds"

        when:
        def result = get(endpoint, TEST_TOKEN)

        then:
        // There is one published build for android app
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.builds', hasSize(2)))
              .andExpect(jsonPath('$.builds[0].permissions.canPublish').value(true))
    }

    def "should get published builds and canPublish flag is set to false"() {
        given:
        def endpoint = "/projects/$TEST_PROJECT_ID/apps/$ANDROID_PLATFORM_NAME/$TEST_APP_ID/builds"

        when:
        def result = get(endpoint, TEST_TOKEN_USER)

        then:
        // There is one published build for android app
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.builds', hasSize(1)))
              .andExpect(jsonPath('$.builds[0].permissions.canPublish').value(false))
    }

    def "should get releaser email even if it is not real user in database"() {
        given:
        def endpoint = "/projects/$TEST_PROJECT_ID/apps/$ANDROID_PLATFORM_NAME/app.id.other/builds"

        when:
        def result = get(endpoint, TEST_TOKEN)

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.builds', hasSize(1)))
              .andExpect(jsonPath('$.builds[0].releaser.email').value('releaser@email.com'))
              .andExpect(jsonPath('$.builds[0].releaser.name').value('releaser@email.com'))
    }

    private static ResultActions assertCommonFieldsCorrectness(ResultActions result) {
        result.andExpect(jsonPath('$.builds[0].version', not(isEmptyString())))
              .andExpect(jsonPath('$.builds[0].releaseNotes', not(isEmptyString())))
              .andExpect(jsonPath('$.builds[0].href', not(isEmptyString())))
              .andExpect(jsonPath('$.builds[0].releaseDate', notNullValue()))
              .andExpect(jsonPath('$.builds[0].isPublished', notNullValue()))
              .andExpect(jsonPath('$.builds[0].isFavorite', notNullValue()))
              .andExpect(jsonPath('$.builds[0].permissions.canPublish', notNullValue()))
              .andExpect(jsonPath('$.builds[0].releaser.email', notNullValue()))
              .andExpect(jsonPath('$.builds[0].releaser.name', notNullValue()))
              .andExpect(jsonPath('$.builds[0].releaser.avatarHref', nullValue()))
              .andExpect(jsonPath('$.builds[0].bytes', notNullValue()))
    }
}
