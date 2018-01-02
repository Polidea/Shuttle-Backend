package com.polidea.shuttle.infrastructure.security.authentication;

import com.polidea.shuttle.error_codes.ErrorCode;
import com.polidea.shuttle.error_codes.ForbiddenException;
import com.polidea.shuttle.error_codes.UnauthorizedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static java.lang.String.format;

/**
 * Gets Access Token from request and lets authentication based on it (if the token is available).
 * Should be explicitly put in the {@link org.springframework.security.web.SecurityFilterChain}.
 *
 * @see com.polidea.shuttle.configuration.SecurityConfiguration
 */
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final String ACCESS_TOKEN_HEADER_NAME = "Access-Token";

    private final AuthenticationManager authenticationManager;

    public TokenAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        PreAuthenticatedAccessToken preAuthenticatedAccessToken = new PreAuthenticatedAccessToken(obtainAccessToken(request));
        Authentication authentication = authenticate(preAuthenticatedAccessToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    private Authentication authenticate(PreAuthenticatedAccessToken preAuthenticatedAccessToken) {
        try {
            return authenticationManager.authenticate(preAuthenticatedAccessToken);
        } catch (InvalidTokenException ex) {
            throw new UnauthorizedException(ErrorCode.INVALID_ACCESS_TOKEN, "Access Token is invalid");
        }
    }

    private String obtainAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(ACCESS_TOKEN_HEADER_NAME))
                       .orElseThrow(MissingAccessTokenException::new);
    }

    private class MissingAccessTokenException extends ForbiddenException {
        MissingAccessTokenException() {
            super(
                ErrorCode.ACCESS_TOKEN_NOT_PROVIDED,
                format("Request does not contain %s header set", ACCESS_TOKEN_HEADER_NAME)
            );
        }
    }
}
