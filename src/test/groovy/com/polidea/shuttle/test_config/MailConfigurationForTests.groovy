package com.polidea.shuttle.test_config

import com.polidea.shuttle.infrastructure.mail.MailAuthService
import com.polidea.shuttle.infrastructure.mail.MailResources
import com.polidea.shuttle.infrastructure.mail.MailService
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import spock.lang.Specification

// According to http://docs.spring.io/spring-boot/docs/1.4.0.RELEASE/reference/html/boot-features-testing.html#boot-features-testing-spring-boot-applications-excluding-config
//   `@TestConfiguration` class has are not picked up by scanning so we can explicitly decide when to use it.
// On the contrary `@Configuration` class will be picked up by scanning by default but it will be not in some cases:
//   eg. if it's subclass of Groovy `Specification` (which is annotated with `@RunWith`).
@TestConfiguration
class MailConfigurationForTests extends Specification {

    @Bean
    MailService mailService() {
        Mock(MailService)
    }

    @Bean
    MailResources mailResources() {
        Mock(MailResources)
    }

    @Bean
    MailAuthService mailAuthService() {
        Mock(MailAuthService)
    }
}
