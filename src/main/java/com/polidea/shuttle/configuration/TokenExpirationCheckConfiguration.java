package com.polidea.shuttle.configuration;

import com.polidea.shuttle.infrastructure.security.authentication.ClientTokenExpirationCheck;
import com.polidea.shuttle.infrastructure.time.TimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenExpirationCheckConfiguration {

    @Autowired
    private TimeService timeService;

    @Value("#{#periodToMillis('${shuttle.tokens.access_token.expiration.period}')}")
    private Long clientTokenExpirationPeriod;

    @Bean
    ClientTokenExpirationCheck clientTokenExpirationCheck() {
        return new ClientTokenExpirationCheck(timeService, clientTokenExpirationPeriod);
    }
}
