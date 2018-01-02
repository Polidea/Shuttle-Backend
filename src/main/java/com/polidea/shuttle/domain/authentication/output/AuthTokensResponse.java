package com.polidea.shuttle.domain.authentication.output;

public class AuthTokensResponse {

    public String accessToken;

    public String refreshToken;

    public AuthTokensResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

}
