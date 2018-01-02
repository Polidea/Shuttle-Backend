package com.polidea.shuttle.test_tools

import com.polidea.shuttle.web.rest.HttpIntegrationSpecification
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment

class TestingToolsSpec extends HttpIntegrationSpecification {

    @Autowired
    ApplicationContext applicationContext

    @Autowired
    Environment environment

    def "should not create TestingToolsController when profile is different than 'testing'"() {
        when:
        applicationContext.getBean(TestingToolsController)

        then:
        thrown(NoSuchBeanDefinitionException)

        and:
        environment.getActiveProfiles().contains('testing') == false
    }

    def "should not create TestingToolsService when profile is different than 'testing'"() {
        when:
        applicationContext.getBean(TestingToolsService)

        then:
        thrown(NoSuchBeanDefinitionException)

        and:
        environment.getActiveProfiles().contains('testing') == false
    }
}
