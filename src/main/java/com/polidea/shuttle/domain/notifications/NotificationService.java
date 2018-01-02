package com.polidea.shuttle.domain.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Iterables;
import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.build.Build;
import com.polidea.shuttle.domain.build.BuildService;
import com.polidea.shuttle.domain.notifications.output.FirebaseNotification;
import com.polidea.shuttle.domain.notifications.output.NotificationAboutNewShuttleVersion;
import com.polidea.shuttle.domain.notifications.output.NotificationAboutProjectAssignment;
import com.polidea.shuttle.domain.notifications.output.NotificationAboutPublishedBuild;
import com.polidea.shuttle.domain.notifications.output.NotificationAboutReleasedBuild;
import com.polidea.shuttle.domain.project.Project;
import com.polidea.shuttle.domain.project.ProjectService;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.UserService;
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toSet;

@Service
@Transactional
public class NotificationService {

    private final NotificationsSenderService notificationsSenderService;
    private final PushTokenService pushTokenService;
    private final UserService userService;
    private final PermissionChecks permissionChecks;
    private final ProjectService projectService;
    private final BuildService buildService;

    private final String androidIconResourceName;
    private final int pushTokensLimit;

    @Autowired
    public NotificationService(NotificationsSenderService notificationsSenderService,
                               PushTokenService pushTokenService,
                               UserService userService,
                               PermissionChecks permissionChecks,
                               ProjectService projectService,
                               BuildService buildService,
                               @Value("${shuttle.notifications.android.icon-resource-name}") String androidIconResourceName,
                               @Value("${shuttle.notifications.push-tokens-limit}") int pushTokensLimit) {
        this.notificationsSenderService = notificationsSenderService;
        this.pushTokenService = pushTokenService;
        this.userService = userService;
        this.permissionChecks = permissionChecks;
        this.projectService = projectService;
        this.buildService = buildService;
        this.androidIconResourceName = androidIconResourceName;
        this.pushTokensLimit = pushTokensLimit;
    }

    public void notifyAboutProjectAssignment(String assigneeEmail, Integer projectId) {
        Project project = projectService.findProject(projectId);
        User assignee = userService.findUser(assigneeEmail);

        if (!project.shouldUserByNotifiedAboutAssignment(assignee)) {
            return;
        }

        Set<String> pushTokens = pushTokensOwnedBy(assignee).stream()
                                                            .map(PushToken::value)
                                                            .collect(toSet());

        if (pushTokens.isEmpty()) {
            return;
        }

        Iterable<List<String>> pushTokensPackets = Iterables.partition(pushTokens, pushTokensLimit);

        pushTokensPackets.forEach(pushTokensPacket ->
                                      send(new NotificationAboutProjectAssignment(
                                          new HashSet<>(pushTokensPacket),
                                          projectId,
                                          project.name(),
                                          androidIconResourceName,
                                          pushTokensLimit
                                      ))
        );
    }

    public void notifyAboutReleasedBuild(Platform platform, String appId, String buildIdentifier) {
        Build build = buildService.findBuild(platform, appId, buildIdentifier);

        Set<User> usersToNotify = build.app().project()
                                       .usersToNotifyAboutReleasedBuild(build)
                                       .stream()
                                       .filter(user -> hasPublishOrViewNotPublishedPermission(user, build.app().project()))
                                       .collect(toSet());

        Set<String> pushTokens = pushTokensOwnedBy(usersToNotify).stream()
                                                                 .filter(pushToken -> pushToken.isFor(platform))
                                                                 .map(PushToken::value)
                                                                 .collect(toSet());

        if (pushTokens.isEmpty()) {
            return;
        }


        Iterable<List<String>> pushTokensPackets = Iterables.partition(pushTokens, pushTokensLimit);

        pushTokensPackets.forEach(pushTokensPacket ->
                                      send(new NotificationAboutReleasedBuild(
                                          new HashSet<>(pushTokensPacket),
                                          build.versionNumber(),
                                          build.app().project().id(),
                                          appId,
                                          build.app().name(),
                                          build.app().iconHref(),
                                          build.href(),
                                          androidIconResourceName,
                                          pushTokensLimit
                                      ))
        );
    }

    public void notifyAboutPublishedBuild(Platform platform, String appId, String buildIdentifier) {
        Build build = buildService.findBuild(platform, appId, buildIdentifier);

        Set<User> usersToNotify = build.app().project().usersToNotifyAboutPublishedBuild(build);

        Set<String> pushTokens = pushTokensOwnedBy(usersToNotify).stream()
                                                                 .filter(pushToken -> pushToken.isFor(platform))
                                                                 .map(PushToken::value)
                                                                 .collect(toSet());

        if (pushTokens.isEmpty()) {
            return;
        }

        Iterable<List<String>> pushTokensPackets = Iterables.partition(pushTokens, pushTokensLimit);

        pushTokensPackets.forEach(pushTokensPacket ->
                                      send(new NotificationAboutPublishedBuild(
                                          new HashSet<>(pushTokensPacket),
                                          build.versionNumber(),
                                          build.app().project().id(),
                                          appId,
                                          build.app().name(),
                                          build.app().iconHref(),
                                          androidIconResourceName,
                                          pushTokensLimit
                                      ))
        );
    }

    public void notifyAboutNewShuttleVersion(Platform platform, String appId, String buildIdentifier) {
        Build build = buildService.findBuild(platform, appId, buildIdentifier);

        Set<User> usersToNotify = userService.fetchAll();

        Set<String> pushTokens = pushTokensOwnedBy(usersToNotify).stream()
                                                                 .filter(pushToken -> pushToken.isFor(platform))
                                                                 .map(PushToken::value)
                                                                 .collect(toSet());

        if (pushTokens.isEmpty()) {
            return;
        }

        Iterable<List<String>> pushTokensPackets = Iterables.partition(pushTokens, pushTokensLimit);

        pushTokensPackets.forEach(pushTokensPacket ->
                                      send(new NotificationAboutNewShuttleVersion(
                                          new HashSet<>(pushTokensPacket),
                                          build.versionNumber(),
                                          build.app().project().id(),
                                          appId,
                                          build.app().name(),
                                          build.app().iconHref(),
                                          androidIconResourceName,
                                          pushTokensLimit
                                      ))
        );
    }

    private void send(FirebaseNotification notification) {
        try {
            notificationsSenderService.send(notification);
        } catch (JsonProcessingException exception) {
            throw new UnableToGenerateNotificationJsonException(exception);
        }
    }

    private Set<PushToken> pushTokensOwnedBy(User user) {
        return pushTokensOwnedBy(newHashSet(user));
    }

    private Set<PushToken> pushTokensOwnedBy(Set<User> users) {
        return users.stream()
                    .flatMap(user -> pushTokenService.findPushTokensOwnedBy(user).stream())
                    .collect(toSet());
    }

    private boolean hasPublishOrViewNotPublishedPermission(User user, Project project) {
        return permissionChecks.check(user)
                               .canPublish(project.id())
                               .or()
                               .canViewNotPublished(project.id())
                               .execute();
    }

}

