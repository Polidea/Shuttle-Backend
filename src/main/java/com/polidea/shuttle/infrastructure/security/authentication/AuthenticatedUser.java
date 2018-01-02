package com.polidea.shuttle.infrastructure.security.authentication;


import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * Holds all necessary data gathered during the authentication process.
 * It can be injected to controller methods by using {@link AuthenticationPrincipal} annotation
 * (in order not to fetch the necessary data multiple times while processing the request).
 */
public class AuthenticatedUser {

    public final String userEmail;
    public final String accessToken;
    public final String deviceId;
    final boolean isGlobalAdmin;

    AuthenticatedUser(String userEmail, String accessToken, String deviceId, boolean isGlobalAdmin) {
        this.userEmail = userEmail;
        this.accessToken = accessToken;
        this.deviceId = deviceId;
        this.isGlobalAdmin = isGlobalAdmin;
    }

}
