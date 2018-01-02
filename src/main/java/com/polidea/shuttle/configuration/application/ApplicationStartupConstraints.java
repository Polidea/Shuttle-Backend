package com.polidea.shuttle.configuration.application;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

@Component
@SuppressWarnings("unused")
class ApplicationStartupConstraints implements ApplicationRunner {

    private static final String PRODUCTION_PROFILE = "production";
    private static final String TESTING_PROFILE = "testing";
    private static final String DEVELOPMENT_PROFILE = "development";
    private static final String INTEGRATION_TESTS_PROFILE = "integrationTests";

    private static final Logger LOGGER = getLogger(ApplicationStartupConstraints.class);

    private final Environment environment;

    @Autowired
    public ApplicationStartupConstraints(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Set<String> contradictoryProfiles =  ImmutableSet.of(
            PRODUCTION_PROFILE,
            TESTING_PROFILE,
            DEVELOPMENT_PROFILE,
            INTEGRATION_TESTS_PROFILE
        );

        Set<String> activeProfiles = ImmutableSet.copyOf(Arrays.asList(environment.getActiveProfiles()));

        if (areProfilesContradictory(
            activeProfiles,
            contradictoryProfiles
        )) {
            LOGGER.error("Contradictory profiles detected. Exiting...");
            throw new ContradictoryProfilesException(activeProfiles);
        }
    }

    private boolean areProfilesContradictory(Set<String> activeProfiles, Set<String> contradictoryProfiles) {
        return Sets.intersection(activeProfiles, contradictoryProfiles).size() > 1;
    }

    class ContradictoryProfilesException extends RuntimeException {
        ContradictoryProfilesException(Set<String> activeProfiles) {
            super(format("Contradictory profiles detected. Active profiles: %s", activeProfiles));
        }
    }

}
