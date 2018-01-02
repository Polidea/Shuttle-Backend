package com.polidea.shuttle.configuration;

import com.polidea.shuttle.infrastructure.time.TimeSpelFunctions;
import com.polidea.shuttle.infrastructure.spel.CustomSpelFunctionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomSpelFunctionConfiguration {

    @Bean
    CustomSpelFunctionProvider customSpelFunctionProvider() {
        return new CustomSpelFunctionProvider(TimeSpelFunctions.class);
    }
}
