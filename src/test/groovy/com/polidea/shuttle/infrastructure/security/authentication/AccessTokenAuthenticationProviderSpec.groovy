package com.polidea.shuttle.infrastructure.security.authentication

import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.UserRepository
import com.polidea.shuttle.domain.user.permissions.global.GlobalPermissionRepository
import com.polidea.shuttle.domain.user.access_token.AccessToken
import com.polidea.shuttle.domain.user.access_token.TokenNotFoundException
import com.polidea.shuttle.domain.user.access_token.AccessTokenService
import spock.lang.Specification

import java.time.Instant

import static com.polidea.shuttle.domain.user.access_token.TokenType.CLIENT

class AccessTokenAuthenticationProviderSpec extends Specification {

    AccessTokenService tokenService = Mock()
    UserRepository userRepository = Mock()
    GlobalPermissionRepository globalPermissionRepository = Mock()
    ClientTokenExpirationCheck clientTokenExpirationCheck = Mock()

    def 'should throw if no token found'() {
        given:
        def tokenValue = 'nonExistentToken'

        and:
        tokenService.findTokenMatching(tokenValue, CLIENT) >> { throw new TokenNotFoundException() }

        and:
        def authenticator = new AccessTokenAuthenticationProvider(tokenService, userRepository, globalPermissionRepository, clientTokenExpirationCheck)

        when:
        authenticator.authenticate(tokenValue)

        then:
        thrown(InvalidTokenException)
    }

    def 'should return user if token found'() {
        given:
        def email = 'not-found-user@domain.com'
        def user = new User(email, 'LOL')

        and:
        def tokenValue = 'existingToken'
        def tokenCreationTimestamp = anyTimestamp()
        def token = new AccessToken(user, 'any-device-id', tokenValue, CLIENT, tokenCreationTimestamp)

        and:
        tokenService.findTokenMatching(tokenValue, CLIENT) >> token
        userRepository.findUser(email) >> Optional.of(user)
        globalPermissionRepository.findFor(user) >> []

        and:
        def authenticator = new AccessTokenAuthenticationProvider(tokenService, userRepository, globalPermissionRepository, clientTokenExpirationCheck)

        when:
        def foundUser = authenticator.authenticate(tokenValue)

        then:
        foundUser.userEmail == email
    }

    Instant anyTimestamp() {
        return Instant.now()
    }

}
