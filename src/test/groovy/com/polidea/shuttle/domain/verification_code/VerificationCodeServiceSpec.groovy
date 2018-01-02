package com.polidea.shuttle.domain.verification_code

import com.polidea.shuttle.domain.user.User
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification

import static java.util.Optional.*

class VerificationCodeServiceSpec extends Specification {

    static final String DEVICE_ID = 'anyDeviceId'
    static final String VERIFICATION_CODE_VALUE = 'validVerificationCode'

    VerificationCodeRepository verificationCodeRepository = Mock(VerificationCodeRepository)
    RandomVerificationCodes randomVerificationCodes = Mock(RandomVerificationCodes)

    VerificationCode givenVerificationCode
    User user

    PasswordEncoder encoder
    VerificationCodeService verificationCodeService

    void setup() {
        user = new User("user@email.com", "User")
        givenVerificationCode = new VerificationCode(DEVICE_ID, user, VERIFICATION_CODE_VALUE)

        encoder = NoOpPasswordEncoder.getInstance()

        verificationCodeService = new VerificationCodeService(
                verificationCodeRepository,
                encoder,
                randomVerificationCodes
        )
    }

    def "should save encoded Verification Code"() {
        given:
        verificationCodeRepository.findByDeviceIdAndUser(DEVICE_ID, user) >> empty()
        randomVerificationCodes.next() >> VERIFICATION_CODE_VALUE

        when:
        verificationCodeService.createRandomVerificationCode(DEVICE_ID, user)

        then:
        1 * verificationCodeRepository.createOrUpdateVerificationCodeWithEmail(DEVICE_ID, user.email(), VERIFICATION_CODE_VALUE)
    }

    def "should encode verification code before saving"() {
        given:
        def encodedCode = 'encodedCode'

        and:
        def mockEncoder = injectMockPasswordEncoderToServiceUnderTest()
        verificationCodeRepository.findByDeviceIdAndUser(DEVICE_ID, user) >> empty()
        randomVerificationCodes.next() >> VERIFICATION_CODE_VALUE

        when:
        verificationCodeService.createRandomVerificationCode(DEVICE_ID, user)

        then:
        1 * mockEncoder.encode(VERIFICATION_CODE_VALUE) >> encodedCode

        then:
        1 * verificationCodeRepository.createOrUpdateVerificationCodeWithEmail(DEVICE_ID, user.email(), encodedCode)
    }

    private PasswordEncoder injectMockPasswordEncoderToServiceUnderTest() {
        def mockEncoder = Mock(PasswordEncoder)
        this.verificationCodeService = new VerificationCodeService(
                verificationCodeRepository,
                mockEncoder,
                randomVerificationCodes
        )
        mockEncoder
    }

    def "should update Verification Code"() {
        given:
        def someOtherCodeValue = 'someOtherCodeValue'

        and:
        verificationCodeRepository.findByDeviceIdAndUser(DEVICE_ID, user) >> of(givenVerificationCode)
        randomVerificationCodes.next() >> someOtherCodeValue

        when:
        verificationCodeService.createRandomVerificationCode(DEVICE_ID, user)

        then:
        1 * verificationCodeRepository.createOrUpdateVerificationCodeWithEmail(DEVICE_ID, user.email(), someOtherCodeValue)
    }

    def "should verify a Verification Code"() {
        given:
        verificationCodeRepository.findByDeviceIdAndUser(DEVICE_ID, user) >> of(givenVerificationCode)

        when:
        verificationCodeService.verifyCode(user, DEVICE_ID, VERIFICATION_CODE_VALUE)

        then:
        encoder.matches(VERIFICATION_CODE_VALUE, givenVerificationCode.encodedValue()) >> true

        and:
        noExceptionThrown()
    }

    def "should delete verification code after successful verification"() {
        given:
        verificationCodeRepository.findByDeviceIdAndUser(DEVICE_ID, user) >> of(givenVerificationCode)

        when:
        verificationCodeService.verifyCode(user, DEVICE_ID, givenVerificationCode.encodedValue())

        then:
        1 * verificationCodeRepository.delete(givenVerificationCode)
    }

    def "verifyCode() should throw exception if there is no valid Verification Code stored"() {
        given:
        verificationCodeRepository.findByDeviceIdAndUser(DEVICE_ID, user) >> empty()

        when:
        verificationCodeService.verifyCode(user, DEVICE_ID, VERIFICATION_CODE_VALUE)

        then:
        thrown(InvalidVerificationCodeException)
    }

    def "verifyCode() should throw exception if Verification Code is invalid"() {
        given:
        verificationCodeRepository.findByDeviceIdAndUser(DEVICE_ID, user) >> of(givenVerificationCode)

        when:
        verificationCodeService.verifyCode(user, DEVICE_ID, 'someOtherCodeValue')

        then:
        thrown(InvalidVerificationCodeException)
    }
}
