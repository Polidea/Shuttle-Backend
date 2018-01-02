package com.polidea.shuttle.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@SuppressWarnings("unused")
public class CorsConfiguration {

    private static final String ADMIN_PANEL_DEVELOPMENT_URL = "http://localhost:9876";

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedMethods("OPTIONS", "GET", "HEAD", "POST", "PUT", "PATCH", "DELETE")
                        .allowedOrigins(ADMIN_PANEL_DEVELOPMENT_URL);
            }
        };
    }

}
