package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.app.App
import com.polidea.shuttle.domain.project.Project

import static com.polidea.shuttle.domain.app.Platform.determinePlatformFromText

class AdminShuttlePublishedBuildsController_HttpSpec extends HttpIntegrationSpecification {

    def releaserEmail = 'releaser@shuttle.com'

    void setup() {
        def shuttleProject = createProject('Shuttle')

        def appIdIos = 'com.polidea.shuttle'
        def platformIos = 'ios'
        createApp(shuttleProject, platformIos, appIdIos, 'Any App')

        def appIdAndroid = 'com.polidea.shuttle'
        def platformAndroid = 'android'
        createApp(shuttleProject, platformAndroid, appIdAndroid, 'Any App')

        createBuild(appIdIos, platformIos, 'ios.prefix.schema.123')
        createBuild(appIdIos, platformAndroid, '123')

        createBuild(appIdIos, platformIos, 'ios.prefix.schema.124', '1.2.4')
        createBuild(appIdIos, platformAndroid, '124', '1.2.4')
    }

    def "should not get latest versions of published Shuttle builds when none were published"() {
        when:
        def userResponse = getWithoutAccessToken("/admin/shuttle/builds/published")

        then:
        userResponse.code() == 200

        userResponse.body().publishedBuilds.android == null
        userResponse.body().publishedBuilds.ios == null
    }

    def "should get latest versions of published Shuttle builds"() {
        when:
        publishBuild('com.polidea.shuttle', 'ios', 'ios.prefix.schema.123')
        publishBuild('com.polidea.shuttle', 'android', '123')
        def userResponse = getWithoutAccessToken("/admin/shuttle/builds/published")

        then:
        userResponse.code() == 200

        userResponse.body().publishedBuilds.android.href != null
        userResponse.body().publishedBuilds.android.qrCodeBase64 != null
        userResponse.body().publishedBuilds.android.version == '1.2.3'

        userResponse.body().publishedBuilds.ios.href != null
        userResponse.body().publishedBuilds.ios.qrCodeBase64 != null
        userResponse.body().publishedBuilds.ios.version == '1.2.3'
    }

    def "should get latest versions of published Shuttle builds when all builds are published"() {
        when:
        publishBuild('com.polidea.shuttle', 'ios', 'ios.prefix.schema.123')
        publishBuild('com.polidea.shuttle', 'android', '123')
        publishBuild('com.polidea.shuttle', 'ios', 'ios.prefix.schema.124')
        publishBuild('com.polidea.shuttle', 'android', '124')
        def userResponse = getWithoutAccessToken("/admin/shuttle/builds/published")

        then:
        userResponse.code() == 200

        userResponse.body().publishedBuilds.android.href != null
        userResponse.body().publishedBuilds.android.qrCodeBase64 != null
        userResponse.body().publishedBuilds.android.version == '1.2.4'

        userResponse.body().publishedBuilds.ios.href != null
        userResponse.body().publishedBuilds.ios.qrCodeBase64 != null
        userResponse.body().publishedBuilds.ios.version == '1.2.4'
    }

    def "should get latest version of published android Shuttle build"() {
        when:
        publishBuild('com.polidea.shuttle', 'android', '123')
        def userResponse = getWithoutAccessToken("/admin/shuttle/builds/published")

        then:
        userResponse.code() == 200

        userResponse.body().publishedBuilds.android.href != null
        userResponse.body().publishedBuilds.android.qrCodeBase64 != null
        userResponse.body().publishedBuilds.android.version == '1.2.3'

        userResponse.body().publishedBuilds.ios == null
    }

    def "should get latest version of published ios Shuttle build"() {
        when:
        publishBuild('com.polidea.shuttle', 'ios', 'ios.prefix.schema.123')
        def userResponse = getWithoutAccessToken("/admin/shuttle/builds/published")

        then:
        userResponse.code() == 200

        userResponse.body().publishedBuilds.android == null

        userResponse.body().publishedBuilds.ios.href != null
        userResponse.body().publishedBuilds.ios.qrCodeBase64 != null
        userResponse.body().publishedBuilds.ios.version == '1.2.3'
    }

    private Project createProject(String name) {
        setupHelper.createProject(name, null)
    }

    private App createApp(Project project, String platform, String appId, String name) {
        setupHelper.createApp(project, determinePlatformFromText(platform), appId, name, null)
    }

    private publishBuild(String appId, String platform, String buildIdentifier) {
        setupHelper.publishBuild(determinePlatformFromText(platform), appId, buildIdentifier);
    }

    private createBuild(String appId, String platform, String buildIdentifier) {
        setupHelper.createBuild(
                appId,
                determinePlatformFromText(platform),
                releaserEmail,
                buildIdentifier,
                '1.2.3',
                'Any Release Notes',
                'http://href.to.app',
                123456789
        )
    }

    private createBuild(String appId, String platform, String buildIdentifier, String buildVersion) {
        setupHelper.createBuild(
                appId,
                determinePlatformFromText(platform),
                releaserEmail,
                buildIdentifier,
                buildVersion,
                'Any Release Notes',
                'http://href.to.app',
                123456789
        )
    }
}

