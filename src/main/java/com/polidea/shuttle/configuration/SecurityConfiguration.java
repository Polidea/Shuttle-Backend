package com.polidea.shuttle.configuration;


import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.polidea.shuttle.infrastructure.security.authentication.AccessTokenAuthenticationProvider;
import com.polidea.shuttle.infrastructure.security.authentication.ClientTokenExpirationCheck;
import com.polidea.shuttle.infrastructure.security.authentication.ContinuousDeploymentAuthenticationProvider;
import com.polidea.shuttle.infrastructure.security.authentication.ExceptionHandlerFilter;
import com.polidea.shuttle.infrastructure.security.authentication.TokenAuthenticationFilter;
import com.polidea.shuttle.infrastructure.time.TimeService;
import com.polidea.shuttle.web.rest.ClientRedirectController;
import com.polidea.shuttle.web.rest.LandingPageController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
// Order ManagementServerProperties.ACCESS_OVERRIDE_ORDER is required to override the Spring Actuator access rules
//   see: http://docs.spring.io/spring-boot/docs/1.4.1.RELEASE/reference/htmlsingle/#boot-features-security-actuator
@Order(ManagementServerProperties.ACCESS_OVERRIDE_ORDER)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    public static final String GLOBAL_ADMIN_ROLE_NAME = "GLOBAL_ADMIN";

    @Autowired
    private ContinuousDeploymentAuthenticationProvider cdAuthenticationProvider;

    @Autowired
    private AccessTokenAuthenticationProvider accessTokenAuthenticationProvider;

    @Bean
    GoogleIdTokenVerifier googleIdTokenVerifier() {
        return new GoogleIdTokenVerifier(new NetHttpTransport(), new JacksonFactory());
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(STATELESS)
            .and()
            .anonymous().disable()
            .addFilterBefore(
                new TokenAuthenticationFilter(authenticationManager()),
                BasicAuthenticationFilter.class
            )
            .addFilterBefore(
                new ExceptionHandlerFilter(),
                TokenAuthenticationFilter.class
            )
            .authorizeRequests()
            .antMatchers("/autoconfig").hasRole(GLOBAL_ADMIN_ROLE_NAME)
            .antMatchers("/beans").hasRole(GLOBAL_ADMIN_ROLE_NAME)
            .antMatchers("/configprops").hasRole(GLOBAL_ADMIN_ROLE_NAME)
            .antMatchers("/env").hasRole(GLOBAL_ADMIN_ROLE_NAME)
            .antMatchers("/flyway").hasRole(GLOBAL_ADMIN_ROLE_NAME)
            .antMatchers("/mappings").hasRole(GLOBAL_ADMIN_ROLE_NAME)
            .antMatchers("/metrics").hasRole(GLOBAL_ADMIN_ROLE_NAME)
            .anyRequest().authenticated();
    }

    @Override
    public void configure(WebSecurity webSecurity) throws Exception {
        webSecurity.ignoring()
                   .antMatchers(HttpMethod.OPTIONS)
                   .antMatchers(
                       "/health",
                       "/admin/shuttle/builds/published",
                       "/auth/code/claim",
                       "/auth/token/claim",
                       "/auth/refresh-token",
                       "/auth/google/login",
                       LandingPageController.LANDING_PAGE_PATH,
                       ClientRedirectController.AUTH_REDIRECT_PATH,
                       ClientRedirectController.DOWNLOAD_REDIRECT_PATH,
                       "/blog",
                       "/testing/**",
                       "/.well-known/apple-app-site-association"
                   );
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .authenticationProvider(accessTokenAuthenticationProvider)
            .authenticationProvider(cdAuthenticationProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
