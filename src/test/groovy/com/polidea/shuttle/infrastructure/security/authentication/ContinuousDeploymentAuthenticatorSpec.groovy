package com.polidea.shuttle.infrastructure.security.authentication

import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.access_token.AccessToken
import com.polidea.shuttle.domain.user.access_token.TokenNotFoundException
import com.polidea.shuttle.domain.user.access_token.AccessTokenService
import spock.lang.Specification

import java.time.Instant

import static com.polidea.shuttle.domain.user.access_token.TokenType.CONTINUOUS_DEPLOYMENT

class ContinuousDeploymentAuthenticatorSpec extends Specification {

    AccessTokenService tokenService = Mock()

    def 'should throw InvalidTokenException if no token found'() {
        given:
        def tokenValue = 'nonExistentToken'
        def authenticator = new ContinuousDeploymentAuthenticationProvider(tokenService)

        and:
        tokenService.findTokenMatching(tokenValue, CONTINUOUS_DEPLOYMENT) >> { throw new TokenNotFoundException() }

        when:
        authenticator.authenticate(tokenValue)

        then:
        thrown(InvalidTokenException)
    }

    def 'should return user if token found'() {
        given:
        def email = 'any.user@domain.com'
        def user = new User(email, 'Any User')

        and:
        def tokenValue = 'existingToken'
        def token = new AccessToken(user, 'any-device-id', tokenValue, CONTINUOUS_DEPLOYMENT, anyTimestamp())
        def authenticator = new ContinuousDeploymentAuthenticationProvider(tokenService)

        and:
        tokenService.findTokenMatching(tokenValue, CONTINUOUS_DEPLOYMENT) >> token

        when:
        def foundUser = authenticator.authenticate(tokenValue)

        then:
        foundUser.userEmail == email
    }

    Instant anyTimestamp() {
        return Instant.now()
    }

}
