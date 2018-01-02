package com.polidea.shuttle.domain.build

import com.polidea.shuttle.domain.app.App
import com.polidea.shuttle.domain.app.input.BuildRequest
import com.polidea.shuttle.domain.build.output.AdminBuildListResponse
import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.UserService
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks
import com.polidea.shuttle.infrastructure.web.WebResources
import spock.lang.Specification

import static com.polidea.shuttle.TestConstants.TEST_APP_ID
import static com.polidea.shuttle.domain.app.Platform.ANDROID

class BuildServiceSpec extends Specification {

    BuildService buildService

    BuildRepository buildRepository = Mock(BuildRepository)
    UserService userService = Mock(UserService)
    WebResources webResources = Mock(WebResources)
    PermissionChecks permissionChecks = Mock(PermissionChecks);

    void setup() {
        buildService = new BuildService(
                buildRepository,
                userService,
                webResources,
                permissionChecks
        )
    }

    def "should return correct BuildListResponse"() {
        when:
        buildRepository.find(ANDROID, TEST_APP_ID) >> [new Build(buildIdentifier: '123', releaser: new User())]
        AdminBuildListResponse response = buildService.fetchAllAppBuilds(TEST_APP_ID, ANDROID)

        then:
        response.builds.size() == 1
    }

    def "should delete build"() {
        given:
        def buildIdentifier = "123"
        def build = new Build(buildIdentifier: buildIdentifier)
        buildRepository.find(ANDROID, TEST_APP_ID, buildIdentifier) >> Optional.of(build)

        when:
        buildService.deleteBuild(ANDROID, TEST_APP_ID, buildIdentifier)

        then:
        1 * buildRepository.delete(build)
    }

    def "should throw EntityNotFoundException when there is no build with such identifier on build deletion"() {
        given:
        def buildIdentifier = "123"
        buildRepository.find(ANDROID, TEST_APP_ID, buildIdentifier) >> Optional.empty()

        when:
        buildService.deleteBuild(ANDROID, TEST_APP_ID, buildIdentifier)

        then:
        0 * buildRepository.delete(_)
        BuildNotFoundException thrownException = thrown()
        thrownException.errorCode.value == 2005
    }

    private void assertSavedBuildCorrectness(Build savedBuild, BuildRequest request, App app) {
        savedBuild.buildIdentifier == request.getBuildIdentifier()
        savedBuild.versionNumber == request.getVersion()
        savedBuild.releaseNotes == request.getReleaseNotes()
        savedBuild.href == request.getHref()
        savedBuild.bytesCount == request.getBytes()
        savedBuild.app == app
        savedBuild.creationTimestamp != null
        !savedBuild.published
    }

}
