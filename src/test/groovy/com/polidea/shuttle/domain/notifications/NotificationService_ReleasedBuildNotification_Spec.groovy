package com.polidea.shuttle.domain.notifications

import com.polidea.shuttle.domain.app.App
import com.polidea.shuttle.domain.app.AppRepository
import com.polidea.shuttle.domain.app.Platform
import com.polidea.shuttle.domain.build.Build
import com.polidea.shuttle.domain.build.BuildNotFoundException
import com.polidea.shuttle.domain.build.BuildRepository
import com.polidea.shuttle.domain.build.BuildService
import com.polidea.shuttle.domain.notifications.output.FirebaseNotification
import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.project.ProjectRepository
import com.polidea.shuttle.domain.project.ProjectService
import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.UserRepository
import com.polidea.shuttle.domain.user.UserService
import com.polidea.shuttle.domain.user.permissions.PermissionType
import com.polidea.shuttle.domain.user.permissions.global.GlobalPermission
import com.polidea.shuttle.domain.user.permissions.global.GlobalPermissionRepository
import com.polidea.shuttle.domain.user.permissions.global.GlobalPermissionsService
import com.polidea.shuttle.domain.user.permissions.project.ProjectPermission
import com.polidea.shuttle.domain.user.permissions.project.ProjectPermissionRepository
import com.polidea.shuttle.domain.user.permissions.project.ProjectPermissionService
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks
import com.polidea.shuttle.infrastructure.web.WebResources
import com.polidea.shuttle.test_config.DummyDatabaseIds
import spock.lang.Specification

import static com.polidea.shuttle.domain.user.permissions.PermissionType.PUBLISHER

class NotificationService_ReleasedBuildNotification_Spec extends Specification {

    public static final String ANDROID_ICON_RESOURCE_NAME = 'some_id_of_icon_resource_on_android'
    public static final int PUSH_TOKENS_LIMIT = 300

    DummyDatabaseIds dummyDatabaseIds = new DummyDatabaseIds()

    NotificationsSenderService notificationsSenderService = Mock(NotificationsSenderService)
    ProjectRepository projectRepository = Mock(ProjectRepository)
    BuildRepository buildRepository = Mock(BuildRepository)
    PushTokenRepository pushTokenRepository = Mock(PushTokenRepository)
    GlobalPermissionRepository globalPermissionRepository = Mock(GlobalPermissionRepository)
    ProjectPermissionRepository projectPermissionRepository = Mock(ProjectPermissionRepository)

    NotificationService notificationService

    Project project
    Platform platform
    Platform anotherPlatform
    String appId
    App app
    String buildIdentifier
    Build build

    FirebaseNotification sentNotificationJson

    void setup() {
        def permissionChecks = new PermissionChecks(
                Stub(UserRepository),
                projectRepository,
                Stub(AppRepository),
                globalPermissionRepository,
                projectPermissionRepository
        )
        def pushTokenService = new PushTokenService(
                pushTokenRepository,
                Stub(UserService)
        )
        def globalPermissionsService = new GlobalPermissionsService(
                globalPermissionRepository,
                Stub(UserRepository)
        )
        def projectPermissionService = new ProjectPermissionService(
                Stub(UserRepository),
                projectRepository,
                projectPermissionRepository
        )
        def projectService = new ProjectService(
                projectRepository,
                Stub(UserRepository),
                projectPermissionService,
                globalPermissionsService,
                permissionChecks
        )
        def buildService = new BuildService(
                buildRepository,
                Stub(UserService),
                Stub(WebResources),
                permissionChecks
        )
        notificationService = new NotificationService(
                notificationsSenderService,
                pushTokenService,
                Stub(UserService),
                permissionChecks,
                projectService,
                buildService,
                ANDROID_ICON_RESOURCE_NAME,
                PUSH_TOKENS_LIMIT
        )
        project = newProject('Some Project')
        projectRepository.findById(project.id) >> Optional.of(project)
        def releaser = newUser('releaser@shuttle.com', 'Build Releaser')
        platform = Platform.ANDROID
        anotherPlatform = Platform.IOS
        appId = 'some.app.id'
        app = newApp(project, platform, appId, 'Some App', 'http://app.icon.href')
        buildIdentifier = 'some.build.identifier'
        build = newBuild(buildIdentifier, '1.2.3', app, releaser, 'http://build.href')
    }

    def "throw exception if there is no Build"() {
        given:
        buildRepository.find(platform, appId, buildIdentifier) >> Optional.empty()

        when:
        notificationService.notifyAboutReleasedBuild(platform, appId, buildIdentifier)

        then:
        thrown(BuildNotFoundException)

        and:
        0 * notificationsSenderService.send(_)
    }

    def "do not notify if there are no Assignees to notify"() {
        given:
        buildRepository.find(platform, appId, buildIdentifier) >> Optional.of(build)

        when:
        notificationService.notifyAboutReleasedBuild(platform, appId, buildIdentifier)

        then:
        0 * notificationsSenderService.send(_)
    }

    def "do not notify if Assignees have no Push Tokens"() {
        given:
        def assignee1 = newUser('assignee1@shuttle.com', '1st Project Assignee')
        def assignee2 = newUser('assignee2@shuttle.com', '2nd Project Assignee')
        project.assign(assignee1)
        project.assign(assignee2)
        userHasAnyPublishPermission(assignee1, project)
        userHasAnyPublishPermission(assignee2, project)

        and:
        buildRepository.find(platform, appId, buildIdentifier) >> Optional.of(build)

        and:
        pushTokenRepository.findTokensOwnedBy(assignee1) >> []
        pushTokenRepository.findTokensOwnedBy(assignee2) >> []

        when:
        notificationService.notifyAboutReleasedBuild(platform, appId, buildIdentifier)

        then:
        0 * notificationsSenderService.send(_)
    }

    def "notify Assignees using all their Push Tokens"() {
        given:
        def assignee1 = newUser('assignee1@shuttle.com', '1st Project Assignee')
        def assignee2 = newUser('assignee2@shuttle.com', '2nd Project Assignee')
        def assignee3 = newUser('assignee2@shuttle.com', '3rd Project Assignee')
        userHasAnyPublishPermission(assignee1, project)
        userHasAnyPublishPermission(assignee2, project)
        userHasAnyPublishPermission(assignee3, project)

        and:
        buildRepository.find(platform, appId, buildIdentifier) >> Optional.of(build)

        and:
        project.assign(assignee1)
        project.assign(assignee2)
        project.assign(assignee3)

        and:
        pushTokenRepository.findTokensOwnedBy(assignee1) >> [newPushToken(assignee1, 'a1pt1', platform), newPushToken(assignee1, 'a1pt2', platform)]
        pushTokenRepository.findTokensOwnedBy(assignee2) >> []
        pushTokenRepository.findTokensOwnedBy(assignee3) >> [newPushToken(assignee3, 'a3pt1', platform)]

        when:
        notificationService.notifyAboutReleasedBuild(platform, appId, buildIdentifier)

        then:
        1 * notificationsSenderService.send(_) >> { arguments -> sentNotificationJson = arguments[0] as FirebaseNotification }
        sentNotificationJson.registrationIds == ['a1pt1', 'a1pt2', 'a3pt1'] as Set
        sentNotificationJson.notificationDetails.bodyKey == 'notification_about_released_build_body'
        sentNotificationJson.notificationDetails.bodyArguments == [build.versionNumber(), app.name()]
        sentNotificationJson.notificationDetails.androidIconResourceName == ANDROID_ICON_RESOURCE_NAME
        sentNotificationJson.customData.notificationType == 'ReleasedBuild'
        sentNotificationJson.customData.projectId == project.id
        sentNotificationJson.customData.appId == appId
        sentNotificationJson.customData.appName == app.name()
        sentNotificationJson.customData.appIconHref == app.iconHref()
        sentNotificationJson.customData.buildHref == build.href()
    }

    def "notify Assignees by partitioning their Push Tokens"() {
        given:
        def numberOfAllPushTokens = 3000
        def assignee1 = newUser('assignee1@shuttle.com', '1st Project Assignee')
        def assignee2 = newUser('assignee2@shuttle.com', '2nd Project Assignee')
        def assignee3 = newUser('assignee2@shuttle.com', '3rd Project Assignee')
        userHasAnyPublishPermission(assignee1, project)
        userHasAnyPublishPermission(assignee2, project)
        userHasAnyPublishPermission(assignee3, project)

        and:
        buildRepository.find(platform, appId, buildIdentifier) >> Optional.of(build)

        and:
        project.assign(assignee1)
        project.assign(assignee2)
        project.assign(assignee3)

        and:
        pushTokenRepository.findTokensOwnedBy(assignee1) >> (1..1000).collect {
            newPushToken(assignee1, it.toString(), platform)
        }
        pushTokenRepository.findTokensOwnedBy(assignee2) >> (1001..2000).collect {
            newPushToken(assignee2, it.toString(), platform)
        }
        pushTokenRepository.findTokensOwnedBy(assignee3) >> (2001..numberOfAllPushTokens).collect {
            newPushToken(assignee3, it.toString(), platform)
        }
        when:
        notificationService.notifyAboutReleasedBuild(platform, appId, buildIdentifier)

        then:
        (numberOfAllPushTokens/PUSH_TOKENS_LIMIT) * notificationsSenderService.send(_) >> _
    }

    def "notify only Assignees who have not muted an App of released Build"() {
        given:
        def assignee1 = newUser('assignee1@shuttle.com', '1st Project Assignee')
        def assignee2 = newUser('assignee2@shuttle.com', '2nd Project Assignee')
        userHasAnyPublishPermission(assignee1, project)
        userHasAnyPublishPermission(assignee2, project)

        and:
        buildRepository.find(platform, appId, buildIdentifier) >> Optional.of(build)

        and:
        project.assign(assignee1)
        project.assign(assignee2)
        assignee1.mute(app)

        and:
        pushTokenRepository.findTokensOwnedBy(assignee1) >> [newPushToken(assignee1, 'a1pt', platform)]
        pushTokenRepository.findTokensOwnedBy(assignee2) >> [newPushToken(assignee1, 'a2pt', platform)]

        when:
        notificationService.notifyAboutReleasedBuild(platform, appId, buildIdentifier)

        then:
        1 * notificationsSenderService.send(_) >> { arguments -> sentNotificationJson = arguments[0] as FirebaseNotification }
        sentNotificationJson.registrationIds == ['a2pt'] as Set
    }

    def "notify only Assignees who have not muted a Project of released Build"() {
        given:
        def assignee1 = newUser('assignee1@shuttle.com', '1st Project Assignee')
        def assignee2 = newUser('assignee2@shuttle.com', '2nd Project Assignee')
        userHasAnyPublishPermission(assignee1, project)
        userHasAnyPublishPermission(assignee2, project)

        and:
        buildRepository.find(platform, appId, buildIdentifier) >> Optional.of(build)

        and:
        project.assign(assignee1)
        project.assign(assignee2)
        assignee1.mute(project)

        and:
        pushTokenRepository.findTokensOwnedBy(assignee1) >> [newPushToken(assignee1, 'a1pt', platform)]
        pushTokenRepository.findTokensOwnedBy(assignee2) >> [newPushToken(assignee1, 'a2pt', platform)]

        when:
        notificationService.notifyAboutReleasedBuild(platform, appId, buildIdentifier)

        then:
        1 * notificationsSenderService.send(_) >> { arguments -> sentNotificationJson = arguments[0] as FirebaseNotification }
        sentNotificationJson.registrationIds == ['a2pt'] as Set
    }

    def "notify only Assignees who have not archived a Project of released Build"() {
        given:
        def assignee1 = newUser('assignee1@shuttle.com', '1st Project Assignee')
        def assignee2 = newUser('assignee2@shuttle.com', '2nd Project Assignee')
        userHasAnyPublishPermission(assignee1, project)
        userHasAnyPublishPermission(assignee2, project)

        and:
        buildRepository.find(platform, appId, buildIdentifier) >> Optional.of(build)

        and:
        pushTokenRepository.findTokensOwnedBy(assignee1) >> [newPushToken(assignee1, 'a1pt', platform)]
        pushTokenRepository.findTokensOwnedBy(assignee2) >> [newPushToken(assignee1, 'a2pt', platform)]

        and:
        project.assign(assignee1)
        project.assign(assignee2)
        assignee1.archive(project)

        when:
        notificationService.notifyAboutReleasedBuild(platform, appId, buildIdentifier)

        then:
        1 * notificationsSenderService.send(_) >> { arguments -> sentNotificationJson = arguments[0] as FirebaseNotification }
        sentNotificationJson.registrationIds == ['a2pt'] as Set
    }

    def "notify only Assignees who have permission to publish Builds"() {
        given:
        def assignee1 = newUser('assignee1@shuttle.com', '1st Project Assignee')
        def assignee2 = newUser('assignee2@shuttle.com', '2nd Project Assignee')
        def assignee3 = newUser('assignee3@shuttle.com', '3rd Project Assignee')
        project.assign(assignee1)
        project.assign(assignee2)
        project.assign(assignee3)

        and:
        buildRepository.find(platform, appId, buildIdentifier) >> Optional.of(build)

        and:
        userHasOnlyGlobalPublishPermission(assignee1)
        userHasNoPublishPermission(assignee2)
        userHasOnlyProjectPublishPermission(assignee3, project)

        and:
        pushTokenRepository.findTokensOwnedBy(assignee1) >> [newPushToken(assignee1, 'a1pt', platform)]
        pushTokenRepository.findTokensOwnedBy(assignee2) >> [newPushToken(assignee2, 'a2pt', platform)]
        pushTokenRepository.findTokensOwnedBy(assignee3) >> [newPushToken(assignee3, 'a3pt', platform)]

        when:
        notificationService.notifyAboutReleasedBuild(platform, appId, buildIdentifier)

        then:
        1 * notificationsSenderService.send(_) >> { arguments -> sentNotificationJson = arguments[0] as FirebaseNotification }
        sentNotificationJson.registrationIds == ['a1pt', 'a3pt'] as Set
    }

    def "notify Assignees using only Push Tokens for Platform of released Build"() {
        given:
        def assignee = newUser('assignee@shuttle.com', 'Project Assignee')
        project.assign(assignee)

        and:
        buildRepository.find(platform, appId, buildIdentifier) >> Optional.of(build)

        and:
        userHasAnyPublishPermission(assignee, project)

        and:
        pushTokenRepository.findTokensOwnedBy(assignee) >> [
                newPushToken(assignee, 'ptOfMatchingPlatform', platform),
                newPushToken(assignee, 'ptOfAnotherPlatform', anotherPlatform)
        ]

        when:
        notificationService.notifyAboutReleasedBuild(platform, appId, buildIdentifier)

        then:
        1 * notificationsSenderService.send(_) >> { arguments -> sentNotificationJson = arguments[0] as FirebaseNotification }
        sentNotificationJson.registrationIds == ['ptOfMatchingPlatform'] as Set
    }

    private User newUser(String email, String name) {
        def user = new User(email, name)
        user.id = dummyDatabaseIds.next()
        return user
    }

    def userHasAnyPublishPermission(User user, Project project) {
        userHasOnlyProjectPublishPermission(user, project)
    }

    def userHasNoPublishPermission(User user) {
        globalPermissionRepository.findFor(user) >> ([] as Set)
        projectPermissionRepository.findByUserAndProject(user, _) >> ([] as Set)
    }

    def userHasOnlyGlobalPublishPermission(User user) {
        globalPermissionRepository.findFor(user) >> ([newGlobalPermission(user, PUBLISHER)] as Set)
        projectPermissionRepository.findByUserAndProject(user, _) >> ([] as Set)
    }

    def userHasOnlyProjectPublishPermission(User user, Project project) {
        globalPermissionRepository.findFor(user) >> ([] as Set)
        projectPermissionRepository.findByUserAndProject(user, project) >> ([newProjectPermission(user, project, PUBLISHER)] as Set)
    }

    private GlobalPermission newGlobalPermission(User user, PermissionType permissionType) {
        def globalPermission = new GlobalPermission(user, permissionType)
        globalPermission.id = dummyDatabaseIds.next()
        return globalPermission
    }

    private ProjectPermission newProjectPermission(User user, Project project, PermissionType permissionType) {
        def projectPermission = new ProjectPermission(user, project, permissionType)
        projectPermission.id = dummyDatabaseIds.next()
        return projectPermission
    }

    private Project newProject(String name) {
        def project = new Project(name)
        project.id = dummyDatabaseIds.next()
        return project
    }

    private App newApp(Project project, Platform platform, String appId, String name, String iconHref) {
        def app = new App(project, platform, appId, name)
        app.setIconHref(iconHref)
        app.id = dummyDatabaseIds.next()
        project.rawApps.add(app)
        return app
    }

    private Build newBuild(String buildIdentifier, String versionNumber, App app, User releaser, String href) {
        def build = new Build(
                buildIdentifier,
                versionNumber,
                'any release notes',
                href,
                123456789,
                app,
                releaser,
                null
        )
        build.id = dummyDatabaseIds.next()
        app.rawBuilds.add(build)
        return build
    }

    private PushToken newPushToken(User owner, String value, Platform platform) {
        def pushToken = new PushToken(owner, platform, 'any-device-id', value)
        pushToken.id = dummyDatabaseIds.next()
        return pushToken
    }

}
