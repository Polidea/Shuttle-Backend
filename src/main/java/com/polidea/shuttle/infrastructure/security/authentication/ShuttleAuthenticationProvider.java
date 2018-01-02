package com.polidea.shuttle.infrastructure.security.authentication;


import org.slf4j.Logger;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import static org.slf4j.LoggerFactory.getLogger;


/**
 * Common {@link AuthenticationProvider} which is based on Access Token.
 */
abstract class ShuttleAuthenticationProvider implements AuthenticationProvider {

    private static final Logger LOGGER = getLogger(ShuttleAuthenticationProvider.class);

    @Override
    final public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PreAuthenticatedAccessToken preAuthenticatedAccessToken = (PreAuthenticatedAccessToken) authentication;
        String tokenValue = preAuthenticatedAccessToken.accessToken();

        LOGGER.info("Verifying token value {}", tokenValue);

        AuthenticatedUser authenticatedUser = authenticate(tokenValue);
        return new ShuttleAuthentication(authenticatedUser);
    }

    /**
     * Performs authentication based on access token provided by {@link TokenAuthenticationFilter}
     *
     * @param tokenValue the access token
     *
     * @return Authenticated user details
     *
     * @throws AuthenticationException should be thrown if authentication fails.
     *
     */
    abstract protected AuthenticatedUser authenticate(String tokenValue) throws AuthenticationException;


    @Override
    public boolean supports(Class<?> preAuthentication) {
        return PreAuthenticatedAccessToken.class.isAssignableFrom(preAuthentication);
    }
}
