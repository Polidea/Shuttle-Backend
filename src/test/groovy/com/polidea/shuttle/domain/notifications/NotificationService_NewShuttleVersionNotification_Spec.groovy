package com.polidea.shuttle.domain.notifications

import com.polidea.shuttle.domain.app.App
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
import com.polidea.shuttle.domain.user.permissions.global.GlobalPermissionsService
import com.polidea.shuttle.domain.user.permissions.project.ProjectPermissionService
import com.polidea.shuttle.infrastructure.avatars.AvatarContentTypeFix
import com.polidea.shuttle.infrastructure.avatars.DefaultAvatars
import com.polidea.shuttle.infrastructure.external_storage.ExternalStorage
import com.polidea.shuttle.infrastructure.external_storage.ExternalStoragePaths
import com.polidea.shuttle.infrastructure.mail.NewUserNotificationMailService
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks
import com.polidea.shuttle.infrastructure.web.WebResources
import com.polidea.shuttle.test_config.DummyDatabaseIds
import spock.lang.Specification

class NotificationService_NewShuttleVersionNotification_Spec extends Specification {

    public static final String ANDROID_ICON_RESOURCE_NAME = 'some_id_of_icon_resource_on_android'
    public static final int PUSH_TOKENS_LIMIT = 300

    DummyDatabaseIds dummyDatabaseIds = new DummyDatabaseIds()

    NotificationsSenderService notificationsSenderService = Mock(NotificationsSenderService)
    BuildRepository buildRepository = Mock(BuildRepository)
    PushTokenRepository pushTokenRepository = Mock(PushTokenRepository)
    UserRepository userRepository = Mock(UserRepository)

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
        def userService = new UserService(
                userRepository,
                Mock(ProjectRepository),
                Mock(DefaultAvatars),
                Mock(GlobalPermissionsService),
                Mock(NewUserNotificationMailService),
                Mock(ProjectPermissionService),
                Mock(ExternalStorage),
                Mock(ExternalStoragePaths),
                Mock(AvatarContentTypeFix)
        )
        def buildService = new BuildService(
                buildRepository,
                Mock(UserService),
                Mock(WebResources),
                Mock(PermissionChecks)
        )
        def pushTokenService = new PushTokenService(
                pushTokenRepository,
                Mock(UserService)
        )
        notificationService = new NotificationService(
                notificationsSenderService,
                pushTokenService,
                userService,
                Mock(PermissionChecks),
                Mock(ProjectService),
                buildService,
                ANDROID_ICON_RESOURCE_NAME,
                PUSH_TOKENS_LIMIT
        )
        project = newProject('Shuttle Project')
        def releaser = newUser('releaser@shuttle.com', 'Build Releaser')
        platform = Platform.IOS
        anotherPlatform = Platform.ANDROID
        appId = 'shuttle.app.id'
        app = newApp(project, platform, appId, 'Some App', 'http://app.icon.href')
        buildIdentifier = 'build.identifier.of.new.shuttle.version'
        build = newBuild(buildIdentifier, '1.2.3', app, releaser)
    }

    def "throw exception if there is no Build"() {
        given:
        buildRepository.find(platform, appId, buildIdentifier) >> Optional.empty()

        when:
        notificationService.notifyAboutNewShuttleVersion(platform, appId, buildIdentifier)

        then:
        thrown(BuildNotFoundException)

        and:
        0 * notificationsSenderService.send(_)
    }

    def "do not notify if there are no Users to notify"() {
        given:
        buildRepository.find(platform, appId, buildIdentifier) >> Optional.of(build)

        and:
        userRepository.allUsers() >> []

        when:
        notificationService.notifyAboutNewShuttleVersion(platform, appId, buildIdentifier)

        then:
        0 * notificationsSenderService.send(_)
    }

    def "do not notify if Users have no Push Tokens"() {
        given:
        buildRepository.find(platform, appId, buildIdentifier) >> Optional.of(build)

        and:
        def user1 = newUser('user1@shuttle.com', '1st User')
        def user2 = newUser('user2@shuttle.com', '2nd User')
        userRepository.allUsers() >> [user1, user2]

        and:
        pushTokenRepository.findTokensOwnedBy(user1) >> []
        pushTokenRepository.findTokensOwnedBy(user2) >> []

        when:
        notificationService.notifyAboutNewShuttleVersion(platform, appId, buildIdentifier)

        then:
        0 * notificationsSenderService.send(_)
    }

    def "notify Users using all their Push Tokens"() {
        given:
        buildRepository.find(platform, appId, buildIdentifier) >> Optional.of(build)

        and:
        def user1 = newUser('user1@shuttle.com', '1st User')
        def user2 = newUser('user2@shuttle.com', '2nd User')
        def user3 = newUser('user3@shuttle.com', '3rd User')
        userRepository.allUsers() >> [user1, user2, user3]

        and:
        pushTokenRepository.findTokensOwnedBy(user1) >> [newPushToken(user1, 'u1pt1', platform), newPushToken(user1, 'u1pt2', platform)]
        pushTokenRepository.findTokensOwnedBy(user2) >> []
        pushTokenRepository.findTokensOwnedBy(user3) >> [newPushToken(user3, 'u3pt1', platform)]

        when:
        notificationService.notifyAboutNewShuttleVersion(platform, appId, buildIdentifier)

        then:
        1 * notificationsSenderService.send(_) >> { arguments -> sentNotificationJson = arguments[0] as FirebaseNotification }
        sentNotificationJson.registrationIds == ['u1pt1', 'u1pt2', 'u3pt1'] as Set
        sentNotificationJson.notificationDetails.bodyKey == 'notification_about_new_shuttle_version_body'
        sentNotificationJson.notificationDetails.bodyArguments == [build.versionNumber(), app.name()]
        sentNotificationJson.notificationDetails.androidIconResourceName == ANDROID_ICON_RESOURCE_NAME
        sentNotificationJson.customData.notificationType == 'NewShuttleVersion'
        sentNotificationJson.customData.projectId == project.id
        sentNotificationJson.customData.appId == appId
        sentNotificationJson.customData.appName == app.name()
        sentNotificationJson.customData.appIconHref == app.iconHref()
    }

    def "notify Users by partitioning their Push Tokens"() {
        given:
        buildRepository.find(platform, appId, buildIdentifier) >> Optional.of(build)

        and:
        def numberOfAllPushTokens = 3000
        def user1 = newUser('user1@shuttle.com', '1st User')
        def user2 = newUser('user2@shuttle.com', '2nd User')
        def user3 = newUser('user3@shuttle.com', '3rd User')
        userRepository.allUsers() >> [user1, user2, user3]

        and:
        pushTokenRepository.findTokensOwnedBy(user1) >> (1..1000).collect {
            newPushToken(user1, it.toString(), platform)
        }
        pushTokenRepository.findTokensOwnedBy(user2) >> (1001..2000).collect {
            newPushToken(user2, it.toString(), platform)
        }
        pushTokenRepository.findTokensOwnedBy(user3) >> (2001..numberOfAllPushTokens).collect {
            newPushToken(user3, it.toString(), platform)
        }

        when:
        notificationService.notifyAboutNewShuttleVersion(platform, appId, buildIdentifier)

        then:
        (numberOfAllPushTokens / PUSH_TOKENS_LIMIT) * notificationsSenderService.send(_) >> _
    }

    def "notify Users using only Push Tokens for Platform of published Build"() {
        given:
        buildRepository.find(platform, appId, buildIdentifier) >> Optional.of(build)

        and:
        def user = newUser('user@shuttle.com', 'Some Shuttle User')
        userRepository.allUsers() >> [user]

        and:
        pushTokenRepository.findTokensOwnedBy(user) >> [
                newPushToken(user, 'ptOfMatchingPlatform', platform),
                newPushToken(user, 'ptOfAnotherPlatform', anotherPlatform)
        ]

        when:
        notificationService.notifyAboutNewShuttleVersion(platform, appId, buildIdentifier)

        then:
        1 * notificationsSenderService.send(_) >> { arguments -> sentNotificationJson = arguments[0] as FirebaseNotification }
        sentNotificationJson.registrationIds == ['ptOfMatchingPlatform'] as Set
    }

    private User newUser(String email, String name) {
        def user = new User(email, name)
        user.id = dummyDatabaseIds.next()
        return user
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

    private Build newBuild(String buildIdentifier, String versionNumber, App app, User releaser) {
        def build = new Build(
                buildIdentifier,
                versionNumber,
                'any release notes',
                'http://any.href',
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
