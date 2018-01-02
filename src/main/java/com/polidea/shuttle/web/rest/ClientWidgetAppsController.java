package com.polidea.shuttle.web.rest;

import com.polidea.shuttle.domain.app.AppService;
import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.app.output.ClientAppListByReleaseDateResponse;
import com.polidea.shuttle.infrastructure.security.authentication.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@SuppressWarnings("unused")
@RestController
public class ClientWidgetAppsController {

    private final AppService appService;

    @Autowired
    public ClientWidgetAppsController(AppService appService) {
        this.appService = appService;
    }

    @RequestMapping(value = "/apps/{platform}/by-release-date", method = GET)
    @ResponseStatus(OK)
    public ClientAppListByReleaseDateResponse getAllAppsByReleaseDate(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                                      @PathVariable Platform platform) {
        return appService.fetchAllAppsByReleaseDate(platform, authenticatedUser.userEmail);
    }
}
