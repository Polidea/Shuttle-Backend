package com.polidea.shuttle.test_config

import com.polidea.shuttle.infrastructure.security.tokens.RandomTokenGenerator
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import spock.lang.Specification

// According to http://docs.spring.io/spring-boot/docs/1.4.0.RELEASE/reference/html/boot-features-testing.html#boot-features-testing-spring-boot-applications-excluding-config
//   `@TestConfiguration` class has are not picked up by scanning so we can explicitly decide when to use it.
// On the contrary `@Configuration` class will be picked up by scanning by default but it will be not in some cases:
//   eg. if it's subclass of Groovy `Specification` (which is annotated with `@RunWith`).
@TestConfiguration
class RandomTokenGeneratorConfigurationForTests extends Specification {

    public static String NEXT_RANDOM_ACCESS_TOKEN_IN_TESTS = 'aCce5s-t0kEn'
    public static String NEXT_RANDOM_REFRESH_TOKEN_IN_TESTS = 'rEfReSh-T0ken'

    @Bean
    RandomTokenGenerator randomAccessTokens() {
        def randomAccessTokens = GroovyMock(RandomTokenGenerator)
        randomAccessTokens.next() >> { NEXT_RANDOM_ACCESS_TOKEN_IN_TESTS }
        randomAccessTokens
    }

    @Bean
    RandomTokenGenerator randomRefreshTokens() {
        def randomRefreshTokens = GroovyMock(RandomTokenGenerator)
        randomRefreshTokens.next() >> { NEXT_RANDOM_REFRESH_TOKEN_IN_TESTS }
        randomRefreshTokens
    }
}
