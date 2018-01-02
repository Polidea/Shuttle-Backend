package com.polidea.shuttle.test_config

import com.polidea.shuttle.infrastructure.external_storage.ExternalStorage
import com.polidea.shuttle.infrastructure.external_storage.ExternalStorageUrl
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import spock.lang.Specification

// According to http://docs.spring.io/spring-boot/docs/1.4.0.RELEASE/reference/html/boot-features-testing.html#boot-features-testing-spring-boot-applications-excluding-config
//   `@TestConfiguration` class has are not picked up by scanning so we can explicitly decide when to use it.
// On the contrary `@Configuration` class will be picked up by scanning by default but it will be not in some cases:
//   eg. if it's subclass of Groovy `Specification` (which is annotated with `@RunWith`).
@TestConfiguration
class ExternalStorageConfigurationForTests extends Specification {

    public static final String UPLOADED_FILE_URL_IN_TESTS = 'https://some.external.storage/uploaded/file.extension'

    @Primary
    @Bean
    ExternalStorage externalStorage() {
        def url = GroovyMock(ExternalStorageUrl)
        url.asText() >> UPLOADED_FILE_URL_IN_TESTS
        def externalStorage = GroovyMock(ExternalStorage)
        externalStorage.uploadFile(_, _, _) >> url
        externalStorage
    }
}
