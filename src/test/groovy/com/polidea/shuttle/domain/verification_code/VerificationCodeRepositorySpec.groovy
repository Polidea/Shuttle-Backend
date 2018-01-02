package com.polidea.shuttle.domain.verification_code

import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.UserRepository
import spock.lang.Specification

import static java.util.Optional.*

class VerificationCodeRepositorySpec extends Specification {

    def userRepository = Mock(UserRepository)
    def verificationCodeJpaRepository = Mock(VerificationCodeJpaRepository)

    VerificationCodeRepository verificationCodeRepository

    void setup() {
        verificationCodeRepository = new VerificationCodeRepository(verificationCodeJpaRepository, userRepository)
    }

    def "should update existing verification code"() {
        given:
        def deviceId = 'deviceId'
        def email = 'email@email.com'
        def newCodeValue = 'newCodeValue'
        def user = new User(id: 1, email: email)

        VerificationCode actualVerificationCode = null

        and:
        userRepository.findUser(email) >> of(user)
        verificationCodeJpaRepository.findByDeviceIdAndUser(deviceId, user) >>
                of(new VerificationCode(deviceId, user, 'otherCodeValue'))

        when:
        verificationCodeRepository.createOrUpdateVerificationCodeWithEmail(deviceId, email, newCodeValue)

        then:
        1 * verificationCodeJpaRepository.save(_) >> {
            arguments -> actualVerificationCode = arguments[0] as VerificationCode
        }

        and:
        actualVerificationCode.user() == user
        actualVerificationCode.deviceId() == deviceId
        actualVerificationCode.encodedValue() == newCodeValue
    }
}
