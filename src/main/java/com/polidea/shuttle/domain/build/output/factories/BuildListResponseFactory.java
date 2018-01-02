package com.polidea.shuttle.domain.build.output.factories;

import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.build.Build;
import com.polidea.shuttle.domain.build.output.AdminBuildListResponse;
import com.polidea.shuttle.domain.build.output.AdminBuildResponse;
import com.polidea.shuttle.domain.build.output.ClientBuildListResponse;
import com.polidea.shuttle.domain.build.output.ClientBuildResponse;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class BuildListResponseFactory {

    private final BuildResponseFactory buildResponseFactory;

    public BuildListResponseFactory(PermissionChecks permissionChecks) {
        this.buildResponseFactory = new BuildResponseFactory(permissionChecks);
    }

    public ClientBuildListResponse createBuildListResponseForClient(Platform platform, Set<Build> builds, User user) {
        List<? extends ClientBuildResponse> buildResponses =
            builds.stream()
                  .map(build -> buildResponseFactory.createClientBuildResponse(platform, build, user))
                  .collect(toList());
        return new ClientBuildListResponse(buildResponses);
    }

    public AdminBuildListResponse createBuildListResponseForAdmin(Platform platform, Set<Build> builds) {
        List<? extends AdminBuildResponse> buildResponses =
            builds.stream()
                  .map(build -> buildResponseFactory.createAdminBuildResponse(platform, build))
                  .collect(toList());
        return new AdminBuildListResponse(buildResponses);
    }

}
