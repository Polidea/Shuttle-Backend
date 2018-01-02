package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.app.AppRepository
import com.polidea.shuttle.domain.build.BuildJpaRepository
import com.polidea.shuttle.domain.build.BuildRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql

import static com.polidea.shuttle.TestConstants.APPS_CONTROLLER_DATA_PATH
import static com.polidea.shuttle.TestConstants.TEST_APP_ID
import static com.polidea.shuttle.TestConstants.TEST_APP_ID_OTHER
import static com.polidea.shuttle.TestConstants.TEST_APP_NAME
import static com.polidea.shuttle.TestConstants.TEST_PROJECT_ID
import static com.polidea.shuttle.TestConstants.TEST_TOKEN
import static com.polidea.shuttle.domain.app.Platform.ANDROID
import static com.polidea.shuttle.domain.app.Platform.IOS
import static com.polidea.shuttle.web.rest.utils.MockMvcAssertions.assertErrorBodyCompletion
import static groovy.json.JsonOutput.toJson
import static org.hamcrest.Matchers.hasSize
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Sql(APPS_CONTROLLER_DATA_PATH)
class AdminAppsController_MockMvcSpec extends MockMvcIntegrationSpecification {

    static final String ENDPOINT = "/admin/projects/$TEST_PROJECT_ID/apps/android/$TEST_APP_ID/"
    static final String ENDPOINT_OTHER = "/admin/projects/$TEST_PROJECT_ID/apps/android/$TEST_APP_ID_OTHER/"

    static final String ENDPOINT_IOS = "/admin/projects/$TEST_PROJECT_ID/apps/ios/$TEST_APP_ID/"

    static
    final VALID_REQUEST = toJson([name: TEST_APP_NAME, iconHref: "http://a.com/a.png"])

    @Autowired
    AppRepository appRepository

    @Autowired
    BuildRepository buildRepository

    @Autowired
    BuildJpaRepository buildJpaRepository

    def "should successfully remove app"() {
        given:
        def endpoint = ENDPOINT_OTHER

        when:
        def result = delete(endpoint, TEST_TOKEN)

        then:
        result.andExpect(status().isNoContent())
        appRepository.find(ANDROID, TEST_APP_ID_OTHER).isPresent() == false
        buildJpaRepository.findAll().count { it.isDeleted() == false } == 2
    }

    def "should edit app name"() {
        given:
        String newAppName = "newAppName"
        def request = toJson([name: newAppName])
        def endpoint = ENDPOINT_OTHER

        when:
        def result = patch(endpoint, request, TEST_TOKEN)

        then:
        result.andExpect(status().isNoContent())
        appRepository.find(ANDROID, TEST_APP_ID_OTHER).get().name == newAppName
    }

    def "should return 409 CONFLICT when the same app is added twice"() {
        when:
        post(ENDPOINT_OTHER, VALID_REQUEST, TEST_TOKEN)
        def result = post(ENDPOINT_OTHER, VALID_REQUEST, TEST_TOKEN)

        then:
        result.andExpect(status().isConflict())
        assertErrorBodyCompletion(result, 409, 2015)
    }

    def "should NOT return 409 CONFLICT when the same named ios and android apps are added"() {
        when:
        post(ENDPOINT, VALID_REQUEST, TEST_TOKEN)
        def result = post(ENDPOINT_IOS, VALID_REQUEST, TEST_TOKEN)

        then:
        result.andExpect(status().isNoContent())

        and:
        appRepository.find(ANDROID, TEST_APP_ID).isPresent() == true
        appRepository.find(IOS, TEST_APP_ID).isPresent() == true
    }

    def "should list existing apps for non admin"() {
        when:
        def result = get("/admin/projects/2/apps/android/", "tokennormal2")

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.apps', hasSize(1)))
    }
}
