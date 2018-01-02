package com.polidea.shuttle.web.rest;

import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.app.input.PushTokenRequest;
import com.polidea.shuttle.domain.device.UnknownDeviceIdException;
import com.polidea.shuttle.domain.notifications.PushTokenService;
import com.polidea.shuttle.infrastructure.security.authentication.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/pushtokens/{platform}")
public class ClientPushTokenController {

    private final PushTokenService pushTokenService;

    @Autowired
    public ClientPushTokenController(PushTokenService pushTokenService) {
        this.pushTokenService = pushTokenService;
    }

    @RequestMapping(method = POST)
    @ResponseStatus(NO_CONTENT)
    public void addPushToken(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                             @PathVariable Platform platform,
                             @RequestBody @Valid PushTokenRequest pushTokenRequest) {
        String deviceId = deviceIdOf(authenticatedUser);
        pushTokenService.addPushToken(
            authenticatedUser.userEmail,
            platform,
            deviceId,
            pushTokenRequest
        );
    }

    private String deviceIdOf(AuthenticatedUser authenticatedUser) {
        String deviceId = authenticatedUser.deviceId;
        if (deviceId != null) {
            return deviceId;
        }
        throw new UnknownDeviceIdException(authenticatedUser.accessToken);
    }

}

