package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.app.App
import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.permissions.PermissionType
import spock.lang.Unroll

import static com.polidea.shuttle.domain.app.Platform.determinePlatformFromText

class ClientShuttleController_HttpSpec extends HttpIntegrationSpecification {

    String anyUserEmail
    String publisherEmail
    String accessToken
    String publisherToken
    Project shuttleProject
    String shuttleAppId

    def setup() {
        shuttleProject = createProject('Shuttle')

        shuttleAppId = 'com.polidea.shuttle'
        createApp(shuttleProject, 'ios', shuttleAppId, 'Shuttle Production')
        createApp(shuttleProject, 'android', shuttleAppId, 'Shuttle Production')

        anyUserEmail = 'any.user@shuttle.com'
        createUser(anyUserEmail, 'Any User')
        accessToken = createClientAccessTokenFor(anyUserEmail)

        publisherEmail = 'any.publisher@shuttle.com'
        createUser(publisherEmail, 'Publisher')
        assignGlobalPermission(publisherEmail, PermissionType.PUBLISHER)
        publisherToken = createClientAccessTokenFor(publisherEmail)
    }

    @Unroll
    def "returns no latest published Shuttle if there are no Builds at all (#platform)"(String platform) {
        when:
        def shuttleResponse = get("/shuttle/${platform}", accessToken)

        then:
        shuttleResponse.code() == 200
        shuttleResponse.body().latestPublished == null

        where:
        platform << ['android', 'ios']
    }

    @Unroll
    def "returns no latest published Shuttle if there are no published Builds (#platform)"(String platform, String buildIdentifier) {
        given:
        createBuild(shuttleAppId, platform, buildIdentifier, '1.2.3', 'http://href.to.app', 123456789)

        when:
        def shuttleResponse = get("/shuttle/${platform}", accessToken)

        then:
        shuttleResponse.code() == 200
        shuttleResponse.body().latestPublished == null

        where:
        platform << ['android', 'ios']
        buildIdentifier << [123456, 'abcdef']
    }

    @Unroll
    def "returns latest published Shuttle (#platform)"(String platform,
                                                       String buildIdentifier1,
                                                       String buildIdentifier2,
                                                       String buildIdentifier3) {
        given:
        createBuild(shuttleAppId, platform, buildIdentifier1, '1.1', 'http://href.to.app/1', 1234567)
        createBuild(shuttleAppId, platform, buildIdentifier2, '1.2', 'http://href.to.app/2', 2345678)
        createBuild(shuttleAppId, platform, buildIdentifier3, '1.3', 'http://href.to.app/3', 3456789)

        and:
        publishBuild(shuttleAppId, platform, buildIdentifier1)
        publishBuild(shuttleAppId, platform, buildIdentifier2)

        when:
        def shuttleResponse = get("/shuttle/${platform}", accessToken)

        then:
        shuttleResponse.code() == 200
        shuttleResponse.body().latestPublished.appId == shuttleAppId
        if (platform == 'android') {
            assert shuttleResponse.body().latestPublished.versionCode == 222
        } else {
            assert shuttleResponse.body().latestPublished.prefixSchema == 'bbb'
        }
        shuttleResponse.body().latestPublished.version == '1.2'
        shuttleResponse.body().latestPublished.releaseDate != null
        shuttleResponse.body().latestPublished.href == 'http://href.to.app/2'
        shuttleResponse.body().latestPublished.bytes == 2345678

        where:
        platform << ['android', 'ios']
        buildIdentifier1 << [111, 'aaa']
        buildIdentifier2 << [222, 'bbb']
        buildIdentifier3 << [333, 'ccc']
    }

    private User createUser(String userEmail, String name) {
        setupHelper.createUser(userEmail, name, null)
    }

    private assignGlobalPermission(String userEmail, PermissionType permission) {
        setupHelper.assignGlobalPermissions(userEmail, [permission])
    }

    private String createClientAccessTokenFor(String userEmail) {
        setupHelper.createClientAccessToken(userEmail, 'any-device-id')
    }

    private Project createProject(String name) {
        setupHelper.createProject(name, null)
    }

    private App createApp(Project project, String platform, String appId, String name) {
        setupHelper.createApp(project, determinePlatformFromText(platform), appId, name, null)
    }

    private createBuild(String appId, String platform, String buildIdentifier, String version, String href, Long bytes) {
        def releaserEmail = 'releaser@shuttle.com'
        setupHelper.createBuild(
                appId,
                determinePlatformFromText(platform),
                releaserEmail,
                buildIdentifier,
                version,
                'Any Release Notes',
                href,
                bytes
        )
    }

    private publishBuild(String appId, String platform, String buildIdentifier) {
        post("/projects/${shuttleProject.id()}/apps/${platform}/${appId}/builds/${buildIdentifier}/publish", publisherToken)
    }

}
