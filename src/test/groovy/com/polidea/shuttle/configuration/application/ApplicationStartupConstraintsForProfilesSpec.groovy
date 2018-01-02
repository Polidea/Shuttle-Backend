package com.polidea.shuttle.configuration.application

import org.springframework.core.env.StandardEnvironment
import spock.lang.Specification

public class ApplicationStartupConstraintsForProfilesSpec extends Specification {

    private ApplicationStartupConstraints applicationConstraints
    private StandardEnvironment environment

    void setup() {
        environment = new StandardEnvironment()
        applicationConstraints = new ApplicationStartupConstraints(environment)
    }

    def "when contradictory profiles [testing, production] are detected should throw exception"() {
        when:
        environment.setActiveProfiles('testing', 'production')
        applicationConstraints.run(null);

        then:
        thrown(ApplicationStartupConstraints.ContradictoryProfilesException)
    }

    def "when contradictory profiles [development, integrationTests, testing] are detected should throw exception"() {
        when:
        environment.setActiveProfiles('development', 'integrationTests', 'testing')
        applicationConstraints.run(null);

        then:
        thrown(ApplicationStartupConstraints.ContradictoryProfilesException)
    }

    def "when no contradictory profiles [production, embeddedPostgres] are detected then should not throw exception"() {
        when:
        environment.setActiveProfiles('development', 'embeddedPostgres')
        applicationConstraints.run(null);

        then:
        notThrown(ApplicationStartupConstraints.ContradictoryProfilesException)
    }
}
