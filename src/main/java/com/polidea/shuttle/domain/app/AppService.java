package com.polidea.shuttle.domain.app;

import com.google.common.collect.Lists;
import com.polidea.shuttle.domain.app.input.AppAdditionRequest;
import com.polidea.shuttle.domain.app.input.AppEditionRequest;
import com.polidea.shuttle.domain.app.output.AdminAppListResponse;
import com.polidea.shuttle.domain.app.output.ClientAppListByReleaseDateResponse;
import com.polidea.shuttle.domain.app.output.ClientAppListResponse;
import com.polidea.shuttle.domain.app.output.factories.AppsListResponseFactory;
import com.polidea.shuttle.domain.build.BuildRepository;
import com.polidea.shuttle.domain.project.Project;
import com.polidea.shuttle.domain.project.ProjectNotFoundException;
import com.polidea.shuttle.domain.project.ProjectRepository;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.UserNotFoundException;
import com.polidea.shuttle.domain.user.UserRepository;
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Iterables.concat;
import static java.util.stream.Collectors.toList;

@Service
@Transactional
public class AppService {

    private final BuildRepository buildRepository;
    private final AppRepository appRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AppsListResponseFactory appsListResponseFactory;
    private final PermissionChecks permissionChecks;

    @Autowired
    public AppService(AppRepository appRepository,
                      ProjectRepository projectRepository,
                      PermissionChecks permissionChecks,
                      BuildRepository buildRepository,
                      UserRepository userRepository) {
        this.appRepository = appRepository;
        this.projectRepository = projectRepository;
        this.buildRepository = buildRepository;
        this.userRepository = userRepository;
        this.appsListResponseFactory = new AppsListResponseFactory(permissionChecks);
        this.permissionChecks = permissionChecks;
    }

    public void addApp(AppAdditionRequest appAdditionRequest, Integer projectId, Platform platform, String appId) {
        Project project = projectRepository.findById(projectId)
                                           .orElseThrow(() -> new ProjectNotFoundException());
        assertNotExists(appId, platform);
        appRepository.createNewApp(
            project,
            platform,
            appId,
            appAdditionRequest.name,
            appAdditionRequest.iconHref
        );
    }

    private void assertNotExists(String appId, Platform platform) {
        Optional<App> foundApp = appRepository.find(platform, appId);
        if (foundApp.isPresent()) {
            throw new DuplicateAppException(appId, platform);
        }
    }

    public App findApp(Platform platform, String appId) {
        return appRepository.find(platform, appId)
                            .orElseThrow(() -> new AppNotFoundException(platform, appId));
    }

    public void mute(String appId, Platform platform, String userEmail) {
        User user = findUser(userEmail);
        App app = findApp(platform, appId);
        user.mute(app);
    }

    public void unmute(String appId, Platform platform, String userEmail) {
        User user = findUser(userEmail);
        App app = findApp(platform, appId);
        user.unmute(app);
    }

    public void delete(String appId, Platform platform) {
        App app = findApp(platform, appId);
        appRepository.delete(app);
    }

    public AdminAppListResponse fetchAllApps(Integer projectId, Platform platform, String userEmail) {
        User user = findUser(userEmail);
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException());
        boolean canViewNotPublished = canViewNotPublished(user, project);
        List<App> apps = new ArrayList<>(appRepository.find(projectId, platform));
        return appsListResponseFactory.createAdminAppListResponse(apps, canViewNotPublished);
    }

    public ClientAppListResponse fetchAllAppsDetails(Integer projectId, Platform platform, String userEmail) {
        User user = findUser(userEmail);
        projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException());
        List<App> apps = appRepository.find(projectId, platform).stream()
                                      .filter(app -> !user.hasArchived(app.project()))
                                      .collect(toList());
        return appsListResponseFactory.createClientAppListResponse(apps, user);
    }

    public void editApp(AppEditionRequest editionRequest, Platform platform, String appId) {
        App app = findApp(platform, appId);
        editApp(app, editionRequest);
    }

    private void editApp(App app, AppEditionRequest editionRequest) {
        if (editionRequest.name() != null) {
            app.setName(editionRequest.name().value());
        }
        if (editionRequest.iconHref() != null) {
            app.setIconHref(editionRequest.iconHref().value());
        }
    }

    private User findUser(String userEmail) {
        return userRepository.findUser(userEmail)
                             .orElseThrow(() -> new UserNotFoundException(userEmail));
    }

    public ClientAppListByReleaseDateResponse fetchAllAppsByReleaseDate(Platform platform, String userEmail) {
        User user = findUser(userEmail);
        Set<Project> projects = projectRepository.projectsOfAssignee(user);
        List<App> appsForViewingNotPublished = fetchAllAppsByReleaseDateForViewingNotPublished(projects, platform, user);
        List<App> appsForViewingPublished = fetchAllAppsByReleaseDateForViewingPublished(projects, platform, user);

        List<App> appsToView = Lists.newArrayList(concat(appsForViewingNotPublished, appsForViewingPublished));

        return appsListResponseFactory.createClientAppListByReleaseDateResponse(appsToView, user);
    }

    private List<App> fetchAllAppsByReleaseDateForViewingNotPublished(Set<Project> projects, Platform platform, User user) {
        return projects.stream()
                       .filter(project -> canViewNotPublished(user, project))
                       .filter(project -> !user.hasArchived(project))
                       .flatMap(project -> appRepository.find(project.id(), platform).stream())
                       .filter(app -> app.lastBuildDate(true).isPresent())
                       .sorted((app1, app2) -> Long.compare(app2.lastBuildDate(true).getAsLong(),
                                                            app1.lastBuildDate(true).getAsLong()))
                       .collect(toList());
    }

    private List<App> fetchAllAppsByReleaseDateForViewingPublished(Set<Project> projects, Platform platform, User user) {
        return projects.stream()
                       .filter(project -> !canViewNotPublished(user, project))
                       .filter(project -> !user.hasArchived(project))
                       .flatMap(project -> appRepository.find(project.id(), platform).stream())
                       .flatMap(app -> buildRepository.findPublished(platform, app.appId()).stream())
                       .sorted((build1, build2) -> Long.compare(build2.releaseDate(), build1.releaseDate()))
                       .map(build -> build.app())
                       .distinct()
                       .collect(toList());
    }

    private Boolean canViewNotPublished(User user, Project project) {
        return permissionChecks.check(user)
                               .canPublish(project.id())
                               .or()
                               .canViewNotPublished(project.id())
                               .execute();
    }
}
