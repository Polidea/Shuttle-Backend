package com.polidea.shuttle.test_config

import com.polidea.shuttle.infrastructure.avatars.Avatar
import com.polidea.shuttle.infrastructure.avatars.DefaultAvatars
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import spock.lang.Specification

// According to http://docs.spring.io/spring-boot/docs/1.4.0.RELEASE/reference/html/boot-features-testing.html#boot-features-testing-spring-boot-applications-excluding-config
//   `@TestConfiguration` class has are not picked up by scanning so we can explicitly decide when to use it.
// On the contrary `@Configuration` class will be picked up by scanning by default but it will be not in some cases:
//   eg. if it's subclass of Groovy `Specification` (which is annotated with `@RunWith`).
@TestConfiguration
class DefaultAvatarsConfigurationForTests extends Specification {

    public static final String NEXT_RANDOM_DEFAULT_AVATAR_URL = 'http://default.avatars.in.tests/random.png'
    public static final String FIRST_DEFAULT_AVATAR_URL = 'http://default.avatars.in.tests/first.png'
    public static final String SECOND_DEFAULT_AVATAR_URL = 'http://default.avatars.in.tests/second.png'

    @Bean
    DefaultAvatars defaultAvatars() {
        def defaultAvatars = GroovyMock(DefaultAvatars)
        defaultAvatars.asList() >> [
                new Avatar(FIRST_DEFAULT_AVATAR_URL),
                new Avatar(SECOND_DEFAULT_AVATAR_URL)
        ]
        defaultAvatars.random() >> new Avatar(NEXT_RANDOM_DEFAULT_AVATAR_URL)
        defaultAvatars
    }

}
