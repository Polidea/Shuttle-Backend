package com.polidea.shuttle.infrastructure.security.authentication;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static com.polidea.shuttle.configuration.SecurityConfiguration.GLOBAL_ADMIN_ROLE_NAME;

/**
 * Wraps {@link AuthenticatedUser} in order to make it accessible by {link {@link AuthenticationPrincipal}}.
 * Also, {@link SimpleGrantedAuthority} are set. Those are to be used in to define access to endpoints.
 *
 * @see com.polidea.shuttle.configuration.SecurityConfiguration
 */
class ShuttleAuthentication extends PreAuthenticatedAuthenticationToken {

    ShuttleAuthentication(AuthenticatedUser authenticatedUser) {
        super(
            authenticatedUser,
            null,
            authenticatedUser.isGlobalAdmin ? globalAdminAuthority() : null
        );
    }

    @Override
    public final boolean isAuthenticated() {
        return true;
    }

    private static Collection<SimpleGrantedAuthority> globalAdminAuthority() {
        SimpleGrantedAuthority globalAdminAuthority = new SimpleGrantedAuthority("ROLE_" + GLOBAL_ADMIN_ROLE_NAME);
        return newArrayList(globalAdminAuthority);
    }

}
