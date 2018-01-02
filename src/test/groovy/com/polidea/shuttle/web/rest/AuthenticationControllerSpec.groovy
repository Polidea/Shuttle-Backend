package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.access_token.AccessTokenJpaRepository
import com.polidea.shuttle.domain.user.refresh_token.RefreshTokenJpaRepository
import com.polidea.shuttle.infrastructure.mail.MailAuthService
import com.polidea.shuttle.test_config.RandomTokenGeneratorConfigurationForTests
import com.polidea.shuttle.test_config.SecurityConfigurationForTests
import org.springframework.beans.factory.annotation.Autowired

import static com.polidea.shuttle.test_config.RandomTokenGeneratorConfigurationForTests.NEXT_RANDOM_ACCESS_TOKEN_IN_TESTS
import static com.polidea.shuttle.test_config.RandomTokenGeneratorConfigurationForTests.NEXT_RANDOM_REFRESH_TOKEN_IN_TESTS
import static com.polidea.shuttle.test_config.RandomVerificationCodesConfigurationForTests.NEXT_RANDOM_VERIFICATION_CODE_IN_TESTS

class AuthenticationControllerSpec extends HttpIntegrationSpecification {

    @Autowired
    MailAuthService mailAuthService
    @Autowired
    AccessTokenJpaRepository accessTokenJpaRepository
    @Autowired
    RefreshTokenJpaRepository refreshTokenJpaRepository

    def "should generate Verification Code and send it in e-mail"() {
        given:
        def userEmail = 'john@user.com'
        createUser(userEmail)

        when:
        def claimCodeResponse = postWithoutAccessToken('/auth/code/claim', [
                deviceId: 'any_device_id',
                email   : userEmail
        ])

        then:
        claimCodeResponse.code() == 204

        and:
        1 * mailAuthService.sendVerificationCode(userEmail, NEXT_RANDOM_VERIFICATION_CODE_IN_TESTS)
    }

    def "should not generate Verification Code for User who does not exist"() {
        given:
        def userEmail = 'no-one@user.com'

        when:
        def claimCodeResponse = postWithoutAccessToken('/auth/code/claim', [
                deviceId: 'any_device_id',
                email   : userEmail
        ])

        then:
        claimCodeResponse.code() == 404
        claimCodeResponse.body().code == 2001
        claimCodeResponse.body().message == "User 'no-one@user.com' does not exist"
    }

    def "should successfully claim Access Token and Refresh Token"() {
        given:
        def userEmail = 'john@user.com'
        def deviceId = 'any_device_id'
        createUser(userEmail)

        and:
        generateVerificationCode('vCode', deviceId, userEmail)

        when:
        def claimTokensResponse = postWithoutAccessToken('/auth/token/claim', [
                deviceId: deviceId,
                email   : userEmail,
                code    : 'vCode'
        ])

        then:
        claimTokensResponse.code() == 200
        claimTokensResponse.body().accessToken == NEXT_RANDOM_ACCESS_TOKEN_IN_TESTS
        claimTokensResponse.body().refreshToken == NEXT_RANDOM_REFRESH_TOKEN_IN_TESTS

        and:
        accessTokenJpaRepository.findAll().count { it.owner.email == userEmail } == 1
        accessTokenJpaRepository.findAll().find { it.owner.email == userEmail }.value == NEXT_RANDOM_ACCESS_TOKEN_IN_TESTS
        accessTokenJpaRepository.findAll().find { it.owner.email == userEmail }.deviceId == deviceId
        refreshTokenJpaRepository.findAll().count { it.owner.email == userEmail } == 1
        refreshTokenJpaRepository.findAll().find {
            it.owner.email == userEmail
        }.value == NEXT_RANDOM_REFRESH_TOKEN_IN_TESTS
        refreshTokenJpaRepository.findAll().find { it.owner.email == userEmail }.deviceId == deviceId
    }

    def "should return 401 UNAUTHORIZED on multiple Verification Code usage"() {
        given:
        def userEmail = 'john@user.com'
        def deviceId = 'deviceId'

        def requestBody = [
                deviceId: deviceId,
                email   : userEmail,
                code    : 'vCode'
        ]
        def path = '/auth/token/claim'

        and:
        createUser(userEmail)
        generateVerificationCode('vCode', deviceId, userEmail)

        when:
        postWithoutAccessToken(path, requestBody)
        def result = postWithoutAccessToken(path, requestBody)

        then:
        result.code() == 401
    }

    def "should successfully refresh token"() {
        given:
        def userEmail = 'john@user.com'
        def deviceId = 'any_device_id'
        def oldRefreshToken = 'oldRefreshToken'
        createUser(userEmail)
        createRefreshToken(userEmail, deviceId, oldRefreshToken)

        when:
        RandomTokenGeneratorConfigurationForTests.NEXT_RANDOM_REFRESH_TOKEN_IN_TESTS = 'new-refresh-token'
        def claimAccessTokenResponse1 = postWithoutAccessToken('/auth/refresh-token', [
                refreshToken: oldRefreshToken
        ])

        then:
        claimAccessTokenResponse1.code() == 200
        claimAccessTokenResponse1.body().accessToken == NEXT_RANDOM_ACCESS_TOKEN_IN_TESTS
        claimAccessTokenResponse1.body().refreshToken == NEXT_RANDOM_REFRESH_TOKEN_IN_TESTS
    }

    def "should not be able to use the same refresh token twice"() {
        given:
        def userEmail = 'john@user.com'
        def deviceId = 'any_device_id'
        def oldRefreshToken = 'oldRefreshToken'
        createUser(userEmail)
        createRefreshToken(userEmail, deviceId, oldRefreshToken)
        createAccessToken(userEmail, deviceId, 'someAccessToken')

        when:
        RandomTokenGeneratorConfigurationForTests.NEXT_RANDOM_REFRESH_TOKEN_IN_TESTS = 'new-temporary-access-token'
        postWithoutAccessToken('/auth/refresh-token', [
                refreshToken: oldRefreshToken
        ])

        then:
        def claimAccessTokenResponse2 = postWithoutAccessToken('/auth/refresh-token', [
                refreshToken: oldRefreshToken
        ])

        and:
        claimAccessTokenResponse2.code() == 401
        claimAccessTokenResponse2.body().code == 2000
    }

    def "should get 401 unauthorized using invalid Refresh Token"() {
        when:
        def claimAccessTokenResponse = postWithoutAccessToken('/auth/refresh-token', [
                refreshToken: 'invalid_refresh_token'
        ])

        then:
        claimAccessTokenResponse.code() == 401
        claimAccessTokenResponse.body().code == 2000
    }

    def "should successfully claim Access Tokens for multiple Users"() {
        given:
        def someUserEmail = 'john@user.com'
        def anotherUserEmail = 'alice@user.com'
        def deviceId = 'any-device'
        createUser(someUserEmail)
        createUser(anotherUserEmail)

        and:
        generateVerificationCode('vCode', deviceId, someUserEmail)
        generateVerificationCode('vCode', deviceId, anotherUserEmail)

        when:
        def claimToken1Response = postWithoutAccessToken('/auth/token/claim', [
                deviceId: deviceId,
                email   : someUserEmail,
                code    : 'vCode'
        ])

        RandomTokenGeneratorConfigurationForTests.NEXT_RANDOM_REFRESH_TOKEN_IN_TESTS = 'new-temporary-refresh-token0'
        RandomTokenGeneratorConfigurationForTests.NEXT_RANDOM_ACCESS_TOKEN_IN_TESTS = 'new-temporary-access-token0'

        def claimToken2Response = postWithoutAccessToken('/auth/token/claim', [
                deviceId: deviceId,
                email   : anotherUserEmail,
                code    : 'vCode'
        ])

        then:
        claimToken1Response.code() == 200
        claimToken2Response.code() == 200

        and:
        accessTokenJpaRepository.findAll().count { it.owner.email == someUserEmail } == 1
        accessTokenJpaRepository.findAll().count { it.owner.email == anotherUserEmail } == 1
    }

    def "should respond with 409 conflict when setting the same accessToken for multiple users"() {
        given:
        def someUserEmail = 'john@user.com'
        def anotherUserEmail = 'alice@user.com'
        def deviceId = 'any-device'
        createUser(someUserEmail)
        createUser(anotherUserEmail)

        and:
        generateVerificationCode('vCode', deviceId, someUserEmail)
        generateVerificationCode('vCode', deviceId, anotherUserEmail)

        when:
        def claimToken1Response = postWithoutAccessToken('/auth/token/claim', [
                deviceId: deviceId,
                email   : someUserEmail,
                code    : 'vCode'
        ])

        RandomTokenGeneratorConfigurationForTests.NEXT_RANDOM_REFRESH_TOKEN_IN_TESTS = 'new-temporary-token'

        def claimToken2Response = postWithoutAccessToken('/auth/token/claim', [
                deviceId: deviceId,
                email   : anotherUserEmail,
                code    : 'vCode'
        ])

        then:
        claimToken1Response.code() == 200
        claimToken2Response.code() == 409
    }

    def "should successfully claim Access Token for multiple devices"() {
        given:
        def userEmail = 'john@user.com'
        def device1 = 'device1'
        def device2 = 'device2'
        createUser(userEmail)

        and:
        generateVerificationCode('vCode1', device1, userEmail)
        generateVerificationCode('vCode2', device2, userEmail)

        when:
        def claimToken1Response = postWithoutAccessToken('/auth/token/claim', [
                deviceId: device1,
                email   : userEmail,
                code    : 'vCode1'
        ])

        RandomTokenGeneratorConfigurationForTests.NEXT_RANDOM_REFRESH_TOKEN_IN_TESTS = 'new-temporary-refresh-token1'
        RandomTokenGeneratorConfigurationForTests.NEXT_RANDOM_ACCESS_TOKEN_IN_TESTS = 'new-temporary-access-token1'

        def claimToken2Response = postWithoutAccessToken('/auth/token/claim', [
                deviceId: device2,
                email   : userEmail,
                code    : 'vCode2'
        ])

        then:
        claimToken1Response.code() == 200
        claimToken2Response.code() == 200

        and:
        accessTokenJpaRepository.findAll().count { it.owner.email == userEmail } == 2
    }

    def "should get 401 unauthorized for User who does not exist"() {
        given:
        def userEmail = 'no-one@user.com'
        def deviceId = 'any_device_id'

        when:
        def claimTokenResponse = postWithoutAccessToken('/auth/token/claim', [
                deviceId: deviceId,
                email   : userEmail,
                code    : 'any-code'
        ])

        then:
        claimTokenResponse.code() == 401
        claimTokenResponse.body().code == 2002
        claimTokenResponse.body().message ==
                "Verification Code 'any-code' is invalid for device '${deviceId}' and user '${userEmail}'"
    }

    def "should not claim Access Token for invalid Verification Code"() {
        given:
        def userEmail = 'john@user.com'
        def deviceId = 'any_device_id'
        createUser(userEmail)

        and:
        generateVerificationCode('validCode', deviceId, userEmail)

        when:
        def invalidVerificationCode = 'invalidCode'
        def claimTokenResponse = postWithoutAccessToken('/auth/token/claim', [
                deviceId: deviceId,
                email   : userEmail,
                code    : invalidVerificationCode
        ])

        then:
        claimTokenResponse.code() == 401
        claimTokenResponse.body().code == 2002
        claimTokenResponse.body().message ==
                "Verification Code '${invalidVerificationCode}' is invalid for device '${deviceId}' and user '${userEmail}'"

        and:
        accessTokenJpaRepository.findAll().count { it.owner.email == userEmail } == 0
    }

    def "should fail to delete invalid Access Token"() {
        given:
        def userEmail = 'john@user.com'
        def deviceId = 'any-device-id'
        createUser(userEmail)

        and:
        generateVerificationCode('validCode', deviceId, userEmail)

        and:
        def claimTokenResponse = postWithoutAccessToken('/auth/token/claim', [
                deviceId: deviceId,
                email   : userEmail,
                code    : 'validCode'
        ])
        assert claimTokenResponse.code() == 200

        when:
        def anotherAccessToken = 'another-access-token'
        def response = delete('/auth/token', anotherAccessToken)

        then:
        response.code() == 401
        response.body().code == 2006
        response.body().message == 'Access Token is invalid'
    }

    def "should fail to delete Access Token if it's not provided"() {
        given:
        def deviceId = 'any_device_id'
        def userEmail = 'john@user.com'
        createUser(userEmail)

        and:
        generateVerificationCode('vCode', deviceId, userEmail)

        and:
        postWithoutAccessToken('/auth/token/claim', [
                deviceId: deviceId,
                email   : userEmail,
                code    : 'vCode'
        ])

        when:
        def response = deleteWithoutAccessToken('/auth/token')

        then:
        response.code() == 403
        response.body().code == 2010
        response.body().message == 'Request does not contain Access-Token header set'
    }

    def "should log in using google token"() {
        given:
        createUser(SecurityConfigurationForTests.GOOGLE_TOKEN_OWNER_IN_TESTS)

        when:
        def response = postWithoutAccessToken('/auth/google/login', [
                token: SecurityConfigurationForTests.GOOGLE_TOKEN_IN_TESTS
        ])

        then:
        response.code() == 200
        response.body().accessToken != null
        response.body().refreshToken != null
    }

    def "should not log in using google token for nonexistent user"() {
        given: 'no users are created'

        when:
        def response = postWithoutAccessToken('/auth/google/login', [
                token: SecurityConfigurationForTests.GOOGLE_TOKEN_IN_TESTS
        ])

        then:
        response.code() == 401
        response.body().code == 2008
    }

    def "should not log in using invalid google token"() {
        given:
        createUser(SecurityConfigurationForTests.GOOGLE_TOKEN_OWNER_IN_TESTS)

        when:
        def response = postWithoutAccessToken('/auth/google/login', [
                token: 'invalidGoogleToken'
        ])

        then:
        response.code() == 401
        response.body().code == 2008
    }

    def "should be able to refresh token after logging in using google"() {
        given:
        createUser(SecurityConfigurationForTests.GOOGLE_TOKEN_OWNER_IN_TESTS)

        def loginResponse = postWithoutAccessToken('/auth/google/login', [
                token: SecurityConfigurationForTests.GOOGLE_TOKEN_IN_TESTS
        ])

        and:
        RandomTokenGeneratorConfigurationForTests.NEXT_RANDOM_ACCESS_TOKEN_IN_TESTS = 'next-random-access-token'

        when:
        def refreshResponse = postWithoutAccessToken('/auth/refresh-token', [
                refreshToken: loginResponse.body().refreshToken
        ])

        then:
        refreshResponse.code() == 200
    }

    User createUser(String userEmail) {
        return setupHelper.createUser(userEmail, "Name of ${userEmail}", null)
    }

    private void createAccessToken(String userEmail, String deviceId, String tokenValue) {
        setupHelper.createAccessToken(userEmail, deviceId, tokenValue)
    }

    private void createRefreshToken(String userEmail, String deviceId, String tokenValue) {
        setupHelper.createRefreshToken(userEmail, deviceId, tokenValue)
    }

    private void generateVerificationCode(String verificationCodeValue, String deviceId, String userEmail) {
        setupHelper.createVerificationCode(verificationCodeValue, deviceId, userEmail)
    }

}
