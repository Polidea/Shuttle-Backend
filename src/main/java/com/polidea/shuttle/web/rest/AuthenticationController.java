package com.polidea.shuttle.web.rest;

import com.polidea.shuttle.domain.authentication.GoogleLogInService;
import com.polidea.shuttle.domain.authentication.TokensService;
import com.polidea.shuttle.domain.authentication.VerificationCodeLogInService;
import com.polidea.shuttle.domain.authentication.input.LogInRequest;
import com.polidea.shuttle.domain.authentication.output.AuthTokensResponse;
import com.polidea.shuttle.domain.notifications.PushTokenService;
import com.polidea.shuttle.domain.user.login.input.MobileAuthenticationCodeRequest;
import com.polidea.shuttle.domain.user.login.input.MobileAuthenticationRefreshTokenRequest;
import com.polidea.shuttle.domain.user.login.input.MobileAuthenticationTokenRequest;
import com.polidea.shuttle.infrastructure.security.authentication.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@SuppressWarnings("unused")
@RestController
public class AuthenticationController {

    private final VerificationCodeLogInService verificationCodeLogInService;

    private final TokensService tokensService;

    private final GoogleLogInService googleLogInService;

    public AuthenticationController(
        VerificationCodeLogInService verificationCodeLogInService,
        TokensService tokensService, PushTokenService pushTokenService,
        GoogleLogInService googleLogInService) {

        this.verificationCodeLogInService = verificationCodeLogInService;
        this.tokensService = tokensService;
        this.googleLogInService = googleLogInService;
    }

    @PostMapping(path = "auth/code/claim")
    @ResponseStatus(NO_CONTENT)
    public void verificationCodeClaim(@RequestBody @Valid MobileAuthenticationCodeRequest request) {
        verificationCodeLogInService.sendNewCodeToEmail(request.deviceId, request.email);
    }

    @PostMapping(path = "auth/token/claim")
    public AuthTokensResponse issueVerificationCode(@RequestBody @Valid MobileAuthenticationTokenRequest request) {
        return verificationCodeLogInService.authenticateByCode(request.deviceId, request.email, request.code);
    }

    @PostMapping(path = "auth/refresh-token")
    public AuthTokensResponse refreshTokens(@RequestBody @Valid MobileAuthenticationRefreshTokenRequest request) {
        return tokensService.refreshTokens(request.refreshToken);
    }

    @PostMapping(path = "auth/google/login")
    public AuthTokensResponse googleLogIn(@RequestBody @Valid LogInRequest request) {
        return googleLogInService.logIn(request.token);
    }

    @DeleteMapping(path = "auth/token")
    @ResponseStatus(NO_CONTENT)
    public void logout(@AuthenticationPrincipal AuthenticatedUser user) {
        tokensService.removeTokens(user.deviceId, user.accessToken);
    }
}
