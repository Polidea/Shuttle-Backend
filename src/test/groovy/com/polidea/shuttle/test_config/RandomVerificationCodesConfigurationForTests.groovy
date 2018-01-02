package com.polidea.shuttle.test_config

import com.polidea.shuttle.domain.verification_code.RandomVerificationCodes
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import spock.lang.Specification

// According to http://docs.spring.io/spring-boot/docs/1.4.0.RELEASE/reference/html/boot-features-testing.html#boot-features-testing-spring-boot-applications-excluding-config
//   `@TestConfiguration` class has are not picked up by scanning so we can explicitly decide when to use it.
// On the contrary `@Configuration` class will be picked up by scanning by default but it will be not in some cases:
//   eg. if it's subclass of Groovy `Specification` (which is annotated with `@RunWith`).
@TestConfiguration
class RandomVerificationCodesConfigurationForTests extends Specification {

    public static final String NEXT_RANDOM_VERIFICATION_CODE_IN_TESTS = 'VC0DE'

    @Bean
    RandomVerificationCodes randomVerificationCodes() {
        def randomVerificationCodes = GroovyMock(RandomVerificationCodes)
        randomVerificationCodes.next() >> NEXT_RANDOM_VERIFICATION_CODE_IN_TESTS
        randomVerificationCodes
    }
}
