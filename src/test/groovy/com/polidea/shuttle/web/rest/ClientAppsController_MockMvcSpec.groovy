package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.project.ProjectService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql

import static com.polidea.shuttle.TestConstants.APPS_USER_CONTROLLER_DATA_PATH
import static com.polidea.shuttle.TestConstants.TEST_PROJECT_ID
import static com.polidea.shuttle.TestConstants.TEST_TOKEN
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_NORMAL_USER
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.isEmptyString
import static org.hamcrest.Matchers.not
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Sql(APPS_USER_CONTROLLER_DATA_PATH)
class ClientAppsController_MockMvcSpec extends MockMvcIntegrationSpecification {

    static final String ANDROID_ENDPOINT = "/projects/$TEST_PROJECT_ID/apps/android"
    static final String IOS_ENDPOINT = "/projects/$TEST_PROJECT_ID/apps/ios"

    @Autowired
    ProjectService projectService

    def "should return app"() {
        when:
        def result = get(ANDROID_ENDPOINT, TEST_TOKEN)

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.apps', hasSize(1)))
              .andExpect(jsonPath('$.apps[0].id', not(isEmptyString())))
              .andExpect(jsonPath('$.apps[0].name', not(isEmptyString())))
              .andExpect(jsonPath('$.apps[0].type', not(isEmptyString())))
              .andExpect(jsonPath('$.apps[0].iconHref', not(isEmptyString())))
              .andExpect(jsonPath('$.apps[0].isMuted', equalTo(false)))
              .andExpect(jsonPath('$.apps[0].permissions.canMute', equalTo(true)))
              .andExpect(jsonPath('$.apps[0].lastReleaseDate', equalTo(0)))

    }

    def "should not return apps from archived project"() {
        when:
        post("/projects/$TEST_PROJECT_ID/archive", TEST_TOKEN_NORMAL_USER)

        and:
        def result = get(ANDROID_ENDPOINT, TEST_TOKEN_NORMAL_USER)

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.apps', hasSize(0)))

    }

    def "should return one app"() {
        when:
        def result = get(IOS_ENDPOINT, TEST_TOKEN)

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.apps', hasSize(1)))
    }

    def "default muted value for app is false"() {
        expect:
        get("/projects/$TEST_PROJECT_ID/apps/android", TEST_TOKEN)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.apps[0].isMuted', equalTo(false)))

    }
}
