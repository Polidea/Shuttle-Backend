package com.polidea.shuttle.application

import com.polidea.shuttle.configuration.application.ApplicationStartupConstraints
import com.polidea.shuttle.web.rest.HttpIntegrationSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationRunner
import org.springframework.context.ApplicationContext

class ShuttleApplicationStartupValidationSpec extends HttpIntegrationSpecification {

    @Autowired
    ApplicationContext applicationContext

    def "ApplicationStartupConstraints should be loaded by Spring"() {
        when:
        def appConstraints = applicationContext.getBean(ApplicationStartupConstraints)

        then:
        appConstraints != null
    }

    def "ApplicationStartupConstraints should implement ApplicationRunner"() {
        when:
        def appConstraints = applicationContext.getBean(ApplicationStartupConstraints)

        then:
        appConstraints in ApplicationRunner
    }
}
