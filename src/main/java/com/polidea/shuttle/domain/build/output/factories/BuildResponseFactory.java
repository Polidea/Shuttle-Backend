package com.polidea.shuttle.domain.build.output.factories;

import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.build.Build;
import com.polidea.shuttle.domain.build.output.AdminAndroidBuildResponse;
import com.polidea.shuttle.domain.build.output.AdminBuildResponse;
import com.polidea.shuttle.domain.build.output.AdminIosBuildResponse;
import com.polidea.shuttle.domain.build.output.ClientAndroidBuildResponse;
import com.polidea.shuttle.domain.build.output.ClientBuildResponse;
import com.polidea.shuttle.domain.build.output.ClientIosBuildResponse;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.output.ClientBuildPermissionsResponse;
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks;

import static java.lang.String.format;

class BuildResponseFactory {

    private final PermissionChecks permissionChecks;

    public BuildResponseFactory(PermissionChecks permissionChecks) {
        this.permissionChecks = permissionChecks;
    }

    ClientBuildResponse createClientBuildResponse(Platform platform, Build build, User user) {
        boolean canPublish = permissionChecks.check(user).canPublish(build.app().project().id()).execute();

        ClientBuildPermissionsResponse permissions = new ClientBuildPermissionsResponse(canPublish);

        if (platform == Platform.ANDROID) {
            return new ClientAndroidBuildResponse(build, user.hasMarkedAsFavorite(build), permissions);
        }
        if (platform == Platform.IOS) {
            return new ClientIosBuildResponse(build, user.hasMarkedAsFavorite(build), permissions);
        }
        throw new UnknownPlatformException(platform);
    }

    AdminBuildResponse createAdminBuildResponse(Platform platform, Build build) {
        if (platform == Platform.ANDROID) {
            return new AdminAndroidBuildResponse(build);
        }
        if (platform == Platform.IOS) {
            return new AdminIosBuildResponse(build);
        }
        throw new UnknownPlatformException(platform);
    }

    private class UnknownPlatformException extends IllegalArgumentException {

        UnknownPlatformException(Platform platform) {
            super(format("There is no such platform: '%s'", platform));
        }

    }
}
