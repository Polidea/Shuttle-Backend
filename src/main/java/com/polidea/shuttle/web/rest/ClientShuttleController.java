package com.polidea.shuttle.web.rest;

import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.build.BuildRepository;
import com.polidea.shuttle.domain.shuttle.output.ShuttleAndroidBuildResponse;
import com.polidea.shuttle.domain.shuttle.output.ShuttleBuildResponse;
import com.polidea.shuttle.domain.shuttle.output.ShuttleBuildsResponse;
import com.polidea.shuttle.domain.shuttle.output.ShuttleIosBuildResponse;
import com.polidea.shuttle.infrastructure.security.authentication.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static com.polidea.shuttle.domain.app.Platform.IOS;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@SuppressWarnings("unused")
@RestController
public class ClientShuttleController {

    private final BuildRepository buildRepository;
    private final String iosAppId;
    private final String androidAppId;

    @Autowired
    public ClientShuttleController(BuildRepository buildRepository,
                                   @Value("${shuttle.app-id.ios}") String iosAppId,
                                   @Value("${shuttle.app-id.android}") String androidAppId) {
        this.buildRepository = buildRepository;
        this.iosAppId = iosAppId;
        this.androidAppId = androidAppId;
    }

    @RequestMapping(value = "/shuttle/{platform}", method = GET)
    @ResponseStatus(OK)
    public ShuttleBuildsResponse getShuttleBuilds(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                  @PathVariable Platform platform) {
        String appId = appIdFor(platform);
        ShuttleBuildResponse buildResponse = buildRepository
            .findNewestPublished(platform, appId)
            .map(build -> platform == IOS
                ? new ShuttleIosBuildResponse(appId, build)
                : new ShuttleAndroidBuildResponse(appId, build))
            .orElse(null);
        return new ShuttleBuildsResponse(buildResponse);
    }

    private String appIdFor(Platform platform) {
        return platform == IOS
            ? iosAppId
            : androidAppId;
    }

}
