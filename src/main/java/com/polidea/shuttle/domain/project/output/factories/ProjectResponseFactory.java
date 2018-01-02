package com.polidea.shuttle.domain.project.output.factories;

import com.polidea.shuttle.domain.app.App;
import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.app.output.ClientAppAndroidLatestBuildResponse;
import com.polidea.shuttle.domain.app.output.ClientAppIosLatestBuildResponse;
import com.polidea.shuttle.domain.build.Build;
import com.polidea.shuttle.domain.project.Project;
import com.polidea.shuttle.domain.project.output.AdminProjectResponse;
import com.polidea.shuttle.domain.project.output.ClientArchivedProjectResponse;
import com.polidea.shuttle.domain.project.output.ClientProjectWithLatestBuildsResponse;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.output.ClientProjectPermissionsResponse;
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks;

import java.util.List;
import java.util.stream.Collectors;

class ProjectResponseFactory {

    private final MemberResponseFactory memberResponseFactory = new MemberResponseFactory();

    private final PermissionChecks permissionChecks;

    ProjectResponseFactory(PermissionChecks permissionChecks) {
        this.permissionChecks = permissionChecks;
    }

    AdminProjectResponse createAdminProjectResponse(Project project, User user) {
        boolean canViewNotPublished = canViewNotPublished(project, user);
        return new AdminProjectResponse(
            project,
            memberResponseFactory.createMemberResponsesFor(project.members()),
            project.lastReleaseDate(canViewNotPublished).orElse(null)
        );
    }

    private Boolean canViewNotPublished(Project project, User user) {
        return permissionChecks.check(user)
                               .canPublish(project.id())
                               .or()
                               .canViewNotPublished(project.id())
                               .execute();
    }

    ClientArchivedProjectResponse createClientArchivedProjectResponse(Project project) {
        return new ClientArchivedProjectResponse(
            project
        );
    }

    ClientProjectWithLatestBuildsResponse createClientProjectWithLatestBuildsResponse(Project project, User user) {
        boolean canMute = permissionChecks.check(user).canMute(project.id()).execute();
        boolean canArchive = permissionChecks.check(user).canArchive(project.id()).execute();
        boolean canViewNotPublished = canViewNotPublished(project, user);

        ClientProjectPermissionsResponse permissions = new ClientProjectPermissionsResponse(canArchive, canMute);

        List<ClientAppAndroidLatestBuildResponse> latestAndroidBuilds =
            project.apps().stream()
                   .filter(app -> app.platform() == Platform.ANDROID)
                   .map(app -> app.lastBuild(canViewNotPublished))
                   .filter(optionalBuild -> optionalBuild.isPresent())
                   .map(optionalBuild -> {
                       @SuppressWarnings("OptionalGetWithoutIsPresent")
                       Build build = optionalBuild.get();
                       App app = build.app();
                       return new ClientAppAndroidLatestBuildResponse(
                           app.appId(),
                           build
                       );
                   })
                   .collect(Collectors.toList());

        List<ClientAppIosLatestBuildResponse> latestIosBuilds =
            project.apps().stream()
                   .filter(app -> app.platform() == Platform.IOS)
                   .map(app -> app.lastBuild(canViewNotPublished))
                   .filter(optionalBuild -> optionalBuild.isPresent())
                   .map(optionalBuild -> {
                       @SuppressWarnings("OptionalGetWithoutIsPresent")
                       Build build = optionalBuild.get();
                       App app = build.app();
                       return new ClientAppIosLatestBuildResponse(
                           app.appId(),
                           build
                       );
                   })
                   .collect(Collectors.toList());

        Long lastReleaseDate = project.lastReleaseDate(canViewNotPublished).orElse(null);

        return new ClientProjectWithLatestBuildsResponse(
            project,
            user.hasMuted(project),
            memberResponseFactory.createMemberResponsesFor(project.members()),
            permissions,
            latestAndroidBuilds,
            latestIosBuilds,
            lastReleaseDate
        );
    }
}
