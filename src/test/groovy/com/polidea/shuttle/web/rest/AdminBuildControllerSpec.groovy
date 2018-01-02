package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.app.Platform
import com.polidea.shuttle.domain.build.BuildRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.ResultActions
import org.springframework.transaction.annotation.Transactional

import static com.polidea.shuttle.TestConstants.ANDROID_PLATFORM_NAME
import static com.polidea.shuttle.TestConstants.BUILD_CONTROLLER_DATA_PATH
import static com.polidea.shuttle.TestConstants.TEST_APP_ID
import static com.polidea.shuttle.TestConstants.TEST_PROJECT_ID
import static com.polidea.shuttle.TestConstants.TEST_TOKEN
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_USER
import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.isEmptyString
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.notNullValue
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Transactional
@Sql(BUILD_CONTROLLER_DATA_PATH)
class AdminBuildControllerSpec extends MockMvcIntegrationSpecification {

    @Autowired
    BuildRepository buildRepository

    def "should get all android app builds"() {
        given:
        def endpoint = "/admin/projects/$TEST_PROJECT_ID/apps/$ANDROID_PLATFORM_NAME/$TEST_APP_ID/builds"

        when:
        def result = get(endpoint, TEST_TOKEN)

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.builds', hasSize(2)))
              .andExpect(jsonPath('$.builds[0].versionCode', not(isEmptyString())))
        assertCommonFieldsCorrectness(result)
    }

    def "should get all android ios builds"() {
        given:
        def endpoint = "/admin/projects/$TEST_PROJECT_ID/apps/ios/$TEST_APP_ID/builds"

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
        def endpoint = "/admin/projects/$TEST_PROJECT_ID/apps/$ANDROID_PLATFORM_NAME/$TEST_APP_ID/builds"

        when:
        def result = get(endpoint, TEST_TOKEN_USER)

        then:
        // There are no published builds for this app
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.builds', hasSize(0)))
    }

    def "should get releaser email even if it is not real user in database"() {
        given:
        def endpoint = "/admin/projects/$TEST_PROJECT_ID/apps/$ANDROID_PLATFORM_NAME/app.id.other/builds"

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
              .andExpect(jsonPath('$.builds[0].bytes', notNullValue()))
    }

    def "should delete build"() {
        given:
        def versionDatabaseId = "123"
        def endpoint = "/admin/projects/$TEST_PROJECT_ID/apps/$ANDROID_PLATFORM_NAME/$TEST_APP_ID/builds/$versionDatabaseId"

        when:
        def result = delete(endpoint, TEST_TOKEN)

        then:
        result.andExpect(status().isNoContent())

        and:
        buildRepository.find(Platform.ANDROID, TEST_APP_ID, versionDatabaseId).isPresent() == false
    }

    def "should delete build which has word after dot in its identifier (truncated by default in Spring)"() {
        given:
        def buildIdentifier = "buildIdentifierWith.wordAfterDot"
        def projectId = 1
        def platformAsText = 'android'
        def platform = Platform.ANDROID
        def appId = 'app.id5'

        when:
        def result = delete("/admin/projects/${projectId}/apps/${platformAsText}/${appId}/builds/$buildIdentifier", TEST_TOKEN)

        then:
        result.andExpect(status().isNoContent())

        and:
        buildRepository.find(platform, appId, buildIdentifier).isPresent() == false
    }

}
