package com.polidea.shuttle.domain.user.login

import com.polidea.shuttle.domain.authentication.TokensService
import com.polidea.shuttle.domain.notifications.PushTokenService
import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.access_token.AccessTokenService
import com.polidea.shuttle.domain.user.refresh_token.RefreshToken
import com.polidea.shuttle.domain.user.refresh_token.RefreshTokenService
import spock.lang.Specification

import static com.polidea.shuttle.domain.user.access_token.TokenType.CLIENT
import static java.util.Optional.of

class TokensServiceSpec extends Specification {

    static final String DEVICE_ID = 'deviceId'

    AccessTokenService accessTokenService = Mock(AccessTokenService)
    RefreshTokenService refreshTokenService = Mock(RefreshTokenService)
    PushTokenService pushTokenService = Stub(PushTokenService)

    TokensService tokenAuthenticationService

    User user

    void setup() {
        user = new User(email: 'any@user.com')
        tokenAuthenticationService = new TokensService(accessTokenService, refreshTokenService, pushTokenService)
    }

    def "should generate Access Token"() {
        when:
        tokenAuthenticationService.generateTokens(user, DEVICE_ID)

        then:
        1 * accessTokenService.saveAccessToken(user, DEVICE_ID, CLIENT)
    }

    def "should generate Refresh Token"() {
        when:
        tokenAuthenticationService.generateTokens(user, DEVICE_ID)

        then:
        1 * refreshTokenService.saveRefreshToken(user, DEVICE_ID)
    }

    def "should refresh access token"() {
        given:
        RefreshToken refreshToken = new RefreshToken(owner: user, deviceId: DEVICE_ID)
        refreshTokenService.findRefreshTokenMatching('tokenValue') >> of(refreshToken)

        when:
        tokenAuthenticationService.refreshTokens('tokenValue')

        then:
        1 * accessTokenService.saveAccessToken(refreshToken.owner(), refreshToken.deviceId(), _)
    }

    def "should refresh the refresh token too"() {
        given:
        RefreshToken refreshToken = new RefreshToken(owner: user, deviceId: DEVICE_ID)
        refreshTokenService.findRefreshTokenMatching('tokenValue') >> of(refreshToken)

        when:
        tokenAuthenticationService.refreshTokens('tokenValue')

        then:
        1 * refreshTokenService.saveRefreshToken(refreshToken.owner(), refreshToken.deviceId())
    }
}
