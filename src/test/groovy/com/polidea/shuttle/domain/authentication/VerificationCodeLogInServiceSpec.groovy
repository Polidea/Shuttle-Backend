package com.polidea.shuttle.domain.authentication

import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.UserNotFoundException
import com.polidea.shuttle.domain.user.UserService
import com.polidea.shuttle.domain.authentication.output.AuthTokensResponse
import com.polidea.shuttle.domain.verification_code.InvalidVerificationCodeException
import com.polidea.shuttle.domain.verification_code.VerificationCodeService
import com.polidea.shuttle.infrastructure.mail.MailAuthService
import spock.lang.Specification
import spock.lang.Unroll

class VerificationCodeLogInServiceSpec extends Specification {

    static final String EMAIL = 'any@user.com'
    static final String DEVICE_ID = 'anyDeviceId'
    static final String VERIFICATION_CODE = 'code'

    private final UserService userService = Stub(UserService)
    private final VerificationCodeService verificationCodeService = Mock(VerificationCodeService)
    private final MailAuthService mailAuthService = Mock(MailAuthService)
    private final TokensService authenticationService = Mock(TokensService)

    private User user
    private VerificationCodeLogInService verificationCodeAuthenticator

    void setup() {
        user = new User(email: EMAIL)
        verificationCodeAuthenticator = new VerificationCodeLogInService(
                userService,
                verificationCodeService,
                mailAuthService,
                authenticationService
        )
    }

    def "should create new Verification Code"() {
        when:
        verificationCodeAuthenticator.sendNewCodeToEmail(DEVICE_ID, EMAIL)

        then:
        1 * verificationCodeService.createRandomVerificationCode(DEVICE_ID, user)
    }

    def "should send email with the verification code to given address"() {
        given:
        def randomCode = 'random code'

        and:
        userService.findUser(EMAIL) >> user

        when:
        verificationCodeAuthenticator.sendNewCodeToEmail(DEVICE_ID, EMAIL)

        then:
        1 * verificationCodeService.createRandomVerificationCode(DEVICE_ID, user) >> randomCode
        1 * mailAuthService.sendVerificationCode(EMAIL, randomCode)
    }

    def "should throw InvalidVerificationCodeException when code owner is not found"() {
        given:
        userService.findUser(EMAIL) >> { throw new UserNotFoundException(EMAIL) }

        when:
        verificationCodeAuthenticator.authenticateByCode(DEVICE_ID, EMAIL, VERIFICATION_CODE)

        then:
        thrown(InvalidVerificationCodeException)
    }

    def "should return generated access and refresh token"() {
        given:
        AuthTokensResponse expectedResponse = new AuthTokensResponse('accessToken', 'refreshToken')

        when:
        AuthTokensResponse returnedTokens = verificationCodeAuthenticator.authenticateByCode(DEVICE_ID, EMAIL, VERIFICATION_CODE)

        then:
        1 * authenticationService.generateTokens(user, DEVICE_ID) >> expectedResponse

        and:
        returnedTokens == expectedResponse
    }

    def "should throw an exception when null Device ID passed to Access Token generation"() {
        when:
        verificationCodeAuthenticator.authenticateByCode(null, EMAIL, VERIFICATION_CODE)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Device ID must not be null'
    }

    @Unroll
    def "should throw an exception when invalid Verification Code ('#invalidVerificationCode') passed to Access Token generation"(String invalidVerificationCode) {
        when:
        verificationCodeAuthenticator.authenticateByCode(DEVICE_ID, EMAIL, invalidVerificationCode)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Verification code must not be null'

        where:
        invalidVerificationCode << [null, '', '\t', '   ', '\n']
    }
}
