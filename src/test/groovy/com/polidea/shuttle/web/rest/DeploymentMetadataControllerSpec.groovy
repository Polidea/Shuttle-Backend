package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.app.AppJpaRepository
import com.polidea.shuttle.domain.build.BuildJpaRepository
import com.polidea.shuttle.domain.build.BuildRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.annotation.Transactional

import static com.polidea.shuttle.TestConstants.ANDROID_PLATFORM_NAME
import static com.polidea.shuttle.TestConstants.DEPLOYMENT_CONTROLLER_DATA_PATH
import static com.polidea.shuttle.TestConstants.IOS_PLATFORM_NAME
import static com.polidea.shuttle.TestConstants.TEST_APP_ID
import static groovy.json.JsonOutput.toJson
import static org.hamcrest.Matchers.equalTo
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Transactional
@Sql(DEPLOYMENT_CONTROLLER_DATA_PATH)
class DeploymentMetadataControllerSpec extends MockMvcIntegrationSpecification {

    @Autowired
    BuildRepository buildRepository

    @Autowired
    BuildJpaRepository buildJpaRepository

    @Autowired
    AppJpaRepository appJpaRepository

    def VALID_ANDROID_REQUEST = toJson(
            [
                    build: [
                            versionCode  : 256,
                            version      : "0.3.0",
                            releaseNotes : "Build release note",
                            href         : "itms-services://xxx.yyy.zzz.pl",
                            bytes        : "123123123",
                            releaserEmail: "email@email.com"
                    ]
            ]
    )

    def VALID_ANDROID_REQUEST_OTHER_BUILD = toJson(
            [
                    build: [
                            versionCode  : 257,
                            version      : "0.3.0",
                            releaseNotes : "Build release note",
                            href         : "itms-services://xxx.yyy.zzz.pl",
                            bytes        : "123123123",
                            releaserEmail: "email@email.com"
                    ]
            ]
    )

    def VALID_IOS_REQUEST = toJson(
            [
                    build: [
                            prefixSchema : "prefixSchema123",
                            version      : "0.3.0",
                            releaseNotes : "Build release note",
                            href         : "itms-services://xxx.yyy.zzz.pl",
                            bytes        : "123123123",
                            releaserEmail: "email@email.com"
                    ]
            ]
    )

    def "should acquire android metadata successfully"() {
        given:
        def endpoint = "/cd/apps/$ANDROID_PLATFORM_NAME/$TEST_APP_ID/builds"

        when:
        def result = post(endpoint, VALID_ANDROID_REQUEST, 'build_creator_token')

        then:
        result.andExpect(status().isOk())

        and:
        appJpaRepository.findAll().count {it.isDeleted() == false} == 2
        buildJpaRepository.findAll().count {it.isDeleted() == false} == 4
    }

    def "should acquire ios metadata successfully"() {
        given:
        def endpoint = "/cd/apps/$IOS_PLATFORM_NAME/$TEST_APP_ID/builds"

        when:
        def result = post(endpoint, VALID_IOS_REQUEST, 'build_creator_token')

        then:
        result.andExpect(status().isOk())

        and:
        appJpaRepository.findAll().count {it.isDeleted() == false} == 2
        buildJpaRepository.findAll().count {it.isDeleted() == false} == 4
    }


    def "should save 2 app builds when gets 2 requests but should not create a new app and icon"() {
        given:
        def endpoint = "/cd/apps/$ANDROID_PLATFORM_NAME/$TEST_APP_ID/builds"

        when:
        post(endpoint, VALID_ANDROID_REQUEST, 'build_creator_token')
        def result = post(endpoint, VALID_ANDROID_REQUEST_OTHER_BUILD, 'build_creator_token')

        then:
        result.andExpect(status().isOk())

        and:
        appJpaRepository.findAll().count {it.isDeleted() == false} == 2
        buildJpaRepository.findAll().count {it.isDeleted() == false} == 5
    }

    def "should return conflict status code when trying to add the same build twice"() {
        given:
        def endpoint = "/cd/apps/$ANDROID_PLATFORM_NAME/$TEST_APP_ID/builds"

        when:
        post(endpoint, VALID_ANDROID_REQUEST, 'build_creator_token')
        def result = post(endpoint, VALID_ANDROID_REQUEST, 'build_creator_token')

        then:
        result.andExpect(status().isConflict())
              .andExpect(jsonPath('$.code', equalTo(2007)))
    }


}
