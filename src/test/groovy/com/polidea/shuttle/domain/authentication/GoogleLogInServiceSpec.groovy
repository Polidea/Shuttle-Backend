package com.polidea.shuttle.domain.authentication

import com.polidea.shuttle.domain.authentication.output.AuthTokensResponse
import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.UserRepository
import com.polidea.shuttle.infrastructure.security.authentication.external_authorities.GoogleTokenVerificationService
import spock.lang.Specification

import static java.util.Optional.empty
import static java.util.Optional.of

class GoogleLogInServiceSpec extends Specification {

    static final String GOOGLE_TOKEN = 'token'

    TokensService tokensService = Mock(TokensService)
    UserRepository userRepository = Mock(UserRepository)
    GoogleTokenVerificationService googleTokenVerificationService = Mock(GoogleTokenVerificationService)

    GoogleLogInService googleLogInService

    User user

    void setup() {
        user = new User()
        googleTokenVerificationService.verifyToken(_) >> of('')
        userRepository.findUser(_) >> of(user)
        googleLogInService = new GoogleLogInService(tokensService, userRepository, googleTokenVerificationService)
    }

    def "should throw InvalidGoogleTokenException when token verification fails"() {
        when:
        googleLogInService.logIn(GOOGLE_TOKEN)

        then:
        1 * googleTokenVerificationService.verifyToken(GOOGLE_TOKEN) >> empty()

        then:
        thrown(InvalidGoogleTokenException)
    }

    def "if user is not found, should throw InvalidGoogleTokenException"() {
        when:
        googleLogInService.logIn(GOOGLE_TOKEN)

        then:
        1 * userRepository.findUser(_) >> empty()

        then:
        thrown(InvalidGoogleTokenException)
    }

    def "should return generated access and refresh token"() {
        given:
        def expectedTokens = new AuthTokensResponse('accessToken', 'refreshToken')

        when:
        AuthTokensResponse tokens = googleLogInService.logIn(GOOGLE_TOKEN)

        then:
        tokensService.generateTokens(_) >> expectedTokens

        then:
        tokens.accessToken == expectedTokens.accessToken
        tokens.refreshToken == expectedTokens.refreshToken
    }
}
