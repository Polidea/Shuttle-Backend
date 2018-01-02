package com.polidea.shuttle.test_config

import com.polidea.shuttle.infrastructure.security.authentication.ClientTokenExpirationCheck
import com.polidea.shuttle.infrastructure.security.authentication.external_authorities.GoogleTokenVerificationService
import com.polidea.shuttle.infrastructure.time.TimeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import spock.lang.Specification

// According to http://docs.spring.io/spring-boot/docs/1.4.0.RELEASE/reference/html/boot-features-testing.html#boot-features-testing-spring-boot-applications-excluding-config
//   `@TestConfiguration` class has are not picked up by scanning so we can explicitly decide when to use it.
// On the contrary `@Configuration` class will be picked up by scanning by default but it will be not in some cases:
//   eg. if it's subclass of Groovy `Specification` (which is annotated with `@RunWith`).
@TestConfiguration
class SecurityConfigurationForTests extends Specification {

    public static final String GOOGLE_TOKEN_OWNER_IN_TESTS = 'google.token.owner.in.tests@shuttle.com'
    public static final String GOOGLE_TOKEN_IN_TESTS = 'google.token.in.tests'

    @Autowired
    TimeService timeService

    private static long _90_DAYS_IN_MILLIS = 7776000000

    @Bean
    GoogleTokenVerificationService googleTokenVerificationService() {
        def googleTokenVerificationService = GroovyMock(GoogleTokenVerificationService)
        googleTokenVerificationService.verifyToken(_) >> { arguments ->
            String token = arguments[0]
            if (token == GOOGLE_TOKEN_IN_TESTS) return Optional.of(GOOGLE_TOKEN_OWNER_IN_TESTS)
            return Optional.empty()
        }
        googleTokenVerificationService
    }

    @Bean
    ClientTokenExpirationCheck clientTokenExpirationCheck() {
        new ClientTokenExpirationCheck(timeService, _90_DAYS_IN_MILLIS)
    }

}
