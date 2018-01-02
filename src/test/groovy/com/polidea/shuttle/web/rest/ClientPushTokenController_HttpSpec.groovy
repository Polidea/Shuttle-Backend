package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.app.Platform
import com.polidea.shuttle.domain.notifications.PushTokenJpaRepository
import com.polidea.shuttle.domain.user.User
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

import static com.polidea.shuttle.domain.app.Platform.determinePlatformFromText

class ClientPushTokenController_HttpSpec extends HttpIntegrationSpecification {

    @Autowired
    PushTokenJpaRepository pushTokenJpaRepository

    String userEmail = 'any.user@shuttle.com'

    void setup() {
        createUser(userEmail)
    }

    @Unroll
    def "should register Push Token (platform: #platform)"(String platform) {
        given:
        def deviceId = 'any-device-id'
        def accessToken = authenticateUser(userEmail, deviceId)
        def pushTokenValue = 'any-push-token'

        when:
        def registerPushTokenResponse = post("/pushtokens/${platform}", [token: pushTokenValue], accessToken)

        then:
        registerPushTokenResponse.code() == 204

        and:
        def registeredPushTokens = pushTokenJpaRepository.findAll()
        registeredPushTokens.size() == 1
        registeredPushTokens[0].deviceId == deviceId
        registeredPushTokens[0].platform == determinePlatformFromText(platform)
        registeredPushTokens[0].value == pushTokenValue

        where:
        platform << ['android', 'ios']
    }

    def "should register Push Tokens for different devices"() {
        given:
        def device1Id = 'device-1'
        def device2Id = 'device-2'
        def accessTokenForDevice1 = authenticateUser(userEmail, device1Id)
        def accessTokenForDevice2 = authenticateUser(userEmail, device2Id)
        def pushTokenValueForDevice1 = 'push-token-of-device-1'
        def pushTokenValueForDevice2 = 'push-token-of-device-2'

        when:
        def registerPushToken1Response = post("/pushtokens/android", [token: pushTokenValueForDevice1], accessTokenForDevice1)
        def registerPushToken2Response = post("/pushtokens/android", [token: pushTokenValueForDevice2], accessTokenForDevice2)

        then:
        registerPushToken1Response.code() == 204
        registerPushToken2Response.code() == 204

        and:
        def registeredPushTokens = pushTokenJpaRepository.findAll()
        registeredPushTokens.size() == 2
        registeredPushTokens.find { it.deviceId == device1Id }.value == pushTokenValueForDevice1
        registeredPushTokens.find { it.deviceId == device2Id }.value == pushTokenValueForDevice2
    }

    def "new Push Tokens registered for same device should overwrite the old one"() {
        given:
        def deviceId = 'any-device'
        def accessToken = authenticateUser(userEmail, deviceId)
        def oldPushTokenValue = 'push-token-of-device-OLD'
        def newPushTokenValue = 'push-token-of-device-NEW'

        and:
        def registerOldPushTokenResponse = post("/pushtokens/android", [token: oldPushTokenValue], accessToken)
        assert registerOldPushTokenResponse.code() == 204

        when:
        def registerNewPushTokenResponse = post("/pushtokens/android", [token: newPushTokenValue], accessToken)
        assert registerNewPushTokenResponse.code() == 204

        then:
        def registeredPushTokens = pushTokenJpaRepository.findAll()
        registeredPushTokens.size() == 1
        registeredPushTokens.find { it.deviceId == deviceId }.value == newPushTokenValue
    }

    def "should delete Push Token on log-out"() {
        given:
        def device1Id = 'device-1'
        def device2Id = 'device-2'
        def accessTokenForDevice1 = authenticateUser(userEmail, device1Id)
        def accessTokenForDevice2 = authenticateUser(userEmail, device2Id)
        def pushTokenValueForDevice1 = 'push-token-of-device-1'
        def pushTokenValueForDevice2 = 'push-token-of-device-2'

        and:
        def registerPushToken1Response = post("/pushtokens/android", [token: pushTokenValueForDevice1], accessTokenForDevice1)
        def registerPushToken2Response = post("/pushtokens/android", [token: pushTokenValueForDevice2], accessTokenForDevice2)
        assert registerPushToken1Response.code() == 204
        assert registerPushToken2Response.code() == 204

        when:
        def logoutResponse = delete('/auth/token', accessTokenForDevice1)
        assert logoutResponse.code() == 204

        then:
        def registeredPushTokens = pushTokenJpaRepository.findAll()
        registeredPushTokens.size() == 1
        registeredPushTokens[0].deviceId == device2Id
        registeredPushTokens[0].platform == Platform.ANDROID
        registeredPushTokens[0].value == pushTokenValueForDevice2
    }

    def "should fail to register Push Token if could not authenticate with Access Token"() {
        given:
        def invalidAccessToken = 'any-invalid-access-token'

        when:
        def registerPushTokenResponse = post("/pushtokens/android", [token: 'any-push-token'], invalidAccessToken)

        then:
        registerPushTokenResponse.code() == 401

        and:
        pushTokenJpaRepository.findAll().size() == 0
    }

    def "should fail to register Push Token if Access Token is not connected with any Device"() {
        given:
        def unknownDeviceId = null
        def accessTokenWithoutDeviceId = authenticateUser(userEmail, unknownDeviceId)

        when:
        def registerPushTokenResponse = post("/pushtokens/android", [token: 'any-push-token'], accessTokenWithoutDeviceId)

        then:
        registerPushTokenResponse.code() == 400
        registerPushTokenResponse.body().code == 2020
        registerPushTokenResponse.body().message ==
                "Cannot register Push Token because Access Token used for authentication doesn't have any Device ID assigned. Access Token: ${accessTokenWithoutDeviceId}"

        and:
        pushTokenJpaRepository.findAll().size() == 0
    }

    private User createUser(String userEmail) {
        setupHelper.createUser(userEmail, "Name of ${userEmail}", null)
    }

    private String authenticateUser(String userEmail, String deviceId) {
        setupHelper.createClientAccessToken(userEmail, deviceId)
    }

}
