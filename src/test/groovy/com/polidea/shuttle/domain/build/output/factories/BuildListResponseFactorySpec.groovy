package com.polidea.shuttle.domain.build.output.factories

import com.polidea.shuttle.domain.build.Build
import com.polidea.shuttle.domain.build.output.ClientBuildListResponse
import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks
import spock.lang.Specification

import static com.polidea.shuttle.TestConstants.TEST_EMAIL
import static com.polidea.shuttle.domain.app.Platform.ANDROID

class BuildListResponseFactorySpec extends Specification {

    public static final Build BUILD_DUMMY = new Build(buildIdentifier: '123', releaser: new User())

    PermissionChecks permissionChecks = Mock(PermissionChecks)

    BuildListResponseFactory buildListResponseFactory

    void setup() {
        buildListResponseFactory = new BuildListResponseFactory(permissionChecks)
    }

    def "should return empty list when no builds are given"() {
        given:
        User user = new User(email: TEST_EMAIL)

        when:
        ClientBuildListResponse result = buildListResponseFactory.createBuildListResponseForClient(
                ANDROID, [].toSet(), user
        )

        then:
        result.builds == []
    }
}
