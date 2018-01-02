package com.polidea.shuttle.domain.app.output.factories;

import com.polidea.shuttle.domain.app.App;
import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.app.output.AdminAppListResponse;
import com.polidea.shuttle.domain.app.output.AdminAppResponse;
import com.polidea.shuttle.domain.app.output.ClientAppByReleaseDateResponse;
import com.polidea.shuttle.domain.app.output.ClientAppListByReleaseDateResponse;
import com.polidea.shuttle.domain.app.output.ClientAppListResponse;
import com.polidea.shuttle.domain.app.output.ClientAppResponse;
import com.polidea.shuttle.domain.app.output.ClientAppWithProjectIdByReleaseDateResponse;
import com.polidea.shuttle.domain.build.Build;
import com.polidea.shuttle.domain.build.output.ClientLatestAndroidBuildResponse;
import com.polidea.shuttle.domain.build.output.ClientLatestBuildResponse;
import com.polidea.shuttle.domain.build.output.ClientLatestIosBuildResponse;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.output.ClientAppPermissionsResponse;
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class AppsListResponseFactory {

    private final PermissionChecks permissionChecks;

    public AppsListResponseFactory(PermissionChecks permissionChecks) {
        this.permissionChecks = permissionChecks;
    }

    private ClientAppPermissionsResponse createUserPermissionsForApp(User user, App app) {
        boolean canMute = permissionChecks.check(user).canMute(app.platform(), app.appId()).execute();
        return new ClientAppPermissionsResponse(canMute);
    }

    public ClientAppListResponse createClientAppListResponse(List<App> apps, User user) {
        List<ClientAppResponse> appResponses =
            apps.stream()
                .map(app -> {
                    boolean canViewNotPublished = canViewNotPublished(user, app.project().id());
                    boolean isMuted = user.hasMuted(app) || user.hasMuted(app.project());
                    ClientAppPermissionsResponse permissions = createUserPermissionsForApp(user, app);

                    Optional<Build> latestBuild = app.lastBuild(canViewNotPublished);

                    return latestBuild
                        .map(build -> createAppResponseWithLastBuildInfo(app,
                                                                         build,
                                                                         isMuted,
                                                                         permissions))
                        .orElseGet(() -> new ClientAppResponse(app, null, isMuted, permissions, null));
                })
                .collect(toList());
        return new ClientAppListResponse(appResponses);
    }


    public ClientAppListByReleaseDateResponse createClientAppListByReleaseDateResponse(List<App> apps, User user) {
        List<ClientAppWithProjectIdByReleaseDateResponse> appsByLastReleaseDate =
            apps.stream()
                .map(app -> {
                    Integer projectId = app.project().id();
                    boolean canViewNotPublished = canViewNotPublished(user, projectId);
                    ClientAppByReleaseDateResponse appByReleaseDate =
                        new ClientAppByReleaseDateResponse(app.appId(),
                                                           app.name(),
                                                           app.iconHref(),
                                                           app.lastBuildDate(canViewNotPublished).getAsLong());
                    return new ClientAppWithProjectIdByReleaseDateResponse(projectId, appByReleaseDate);
                })
                .collect(Collectors.toList());
        return new ClientAppListByReleaseDateResponse(appsByLastReleaseDate);
    }

    public AdminAppListResponse createAdminAppListResponse(List<App> apps, boolean userCanViewNotPublished) {
        List<AdminAppResponse> appResponses =
            apps.stream()
                .map(app -> new AdminAppResponse(app, userCanViewNotPublished))
                .collect(toList());
        return new AdminAppListResponse(appResponses);
    }

    private ClientAppResponse createAppResponseWithLastBuildInfo(App app,
                                                                 Build latestBuild,
                                                                 boolean isMuted,
                                                                 ClientAppPermissionsResponse permissions) {
        if (app.platform() == Platform.ANDROID) {
            ClientLatestBuildResponse latestBuildResponse =
                new ClientLatestAndroidBuildResponse(latestBuild);
            return new ClientAppResponse(app,
                                         latestBuildResponse,
                                         isMuted,
                                         permissions,
                                         latestBuild.releaseDate());
        } else {
            ClientLatestBuildResponse latestBuildResponse =
                new ClientLatestIosBuildResponse(latestBuild);
            return new ClientAppResponse(app,
                                         latestBuildResponse,
                                         isMuted,
                                         permissions,
                                         latestBuild.releaseDate());
        }
    }

    private Boolean canViewNotPublished(User user, Integer projectId) {
        return permissionChecks.check(user)
                               .canPublish(projectId)
                               .or()
                               .canViewNotPublished(projectId)
                               .execute();
    }
}
