package com.polidea.shuttle.test_config

import com.polidea.shuttle.infrastructure.time.TimeService
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import spock.lang.Specification

import java.time.Instant

// According to http://docs.spring.io/spring-boot/docs/1.4.0.RELEASE/reference/html/boot-features-testing.html#boot-features-testing-spring-boot-applications-excluding-config
//   `@TestConfiguration` class has are not picked up by scanning so we can explicitly decide when to use it.
// On the contrary `@Configuration` class will be picked up by scanning by default but it will be not in some cases:
//   eg. if it's subclass of Groovy `Specification` (which is annotated with `@RunWith`).
@TestConfiguration
class TimeServiceConfigurationForTests extends Specification {

    public static final Instant CURRENT_TIME_IN_TESTS = Instant.parse('2000-01-01T00:00:00Z')

    @Bean
    TimeService timeService() {
        def timeService = GroovyMock(TimeService)
        timeService.currentTime() >> CURRENT_TIME_IN_TESTS
        timeService
    }
}
