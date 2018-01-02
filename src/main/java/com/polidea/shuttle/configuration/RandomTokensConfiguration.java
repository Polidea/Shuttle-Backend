package com.polidea.shuttle.configuration;

import com.polidea.shuttle.infrastructure.security.tokens.RandomTokenGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RandomTokensConfiguration {

    private static final int LENGTH = 2048;

    @Bean
    RandomTokenGenerator tokenGenerator() {
        return new RandomTokenGenerator(LENGTH);
    }


}
