package com.polidea.shuttle.infrastructure.security.authentication

import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.access_token.AccessToken
import com.polidea.shuttle.domain.user.access_token.TokenType
import com.polidea.shuttle.infrastructure.time.TimeService
import spock.lang.Specification

import java.time.Instant
import java.time.LocalDateTime

import static java.time.ZoneOffset.UTC

class ClientAccessTokenExpirationCheckSpec extends Specification {

    private Instant currentTimestamp = LocalDateTime.now().toInstant(UTC)

    TimeService timeService = GroovyMock(TimeService) {
        currentTime() >> currentTimestamp
    }
    private ClientTokenExpirationCheck storeTokenExpirationCheck

    void setup() {
        storeTokenExpirationCheck = new ClientTokenExpirationCheck(timeService, 120000)
    }

    def "should accept Store token"() {
        given:
        def token = anyValidTokenOfType(TokenType.CLIENT)

        when:
        storeTokenExpirationCheck.hasExpired(token)

        then:
        noExceptionThrown()
    }

    def "should not accept non-Store token"() {
        given:
        def token = anyValidTokenOfType(tokenType as TokenType)

        when:
        storeTokenExpirationCheck.hasExpired(token)

        then:
        def thrownException = thrown(IllegalArgumentException)
        thrownException.message == "Only ${TokenType.CLIENT} tokens are allowed." as String

        where:
        tokenType << TokenType.values() - TokenType.CLIENT
    }

    private AccessToken anyValidTokenOfType(TokenType type) {
        def nonExpiredCreationTimestamp = currentTimestamp
        return new AccessToken(new User('any@user', 'Any User'), 'any-device-id', 'anyToken', type, nonExpiredCreationTimestamp)
    }
}
