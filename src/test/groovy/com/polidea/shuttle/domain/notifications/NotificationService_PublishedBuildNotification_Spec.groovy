package com.polidea.shuttle.domain.notifications

import com.polidea.shuttle.domain.app.App
import com.polidea.shuttle.domain.app.Platform
import com.polidea.shuttle.domain.build.Build
import com.polidea.shuttle.domain.build.BuildNotFoundException
import com.polidea.shuttle.domain.build.BuildRepository
import com.polidea.shuttle.domain.build.BuildService
import com.polidea.shuttle.domain.notifications.output.FirebaseNotification
import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.project.ProjectService
import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.UserService
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks
import com.polidea.shuttle.infrastructure.web.WebResources
import com.polidea.shuttle.test_config.DummyDatabaseIds
import spock.lang.Specification

class NotificationService_PublishedBuildNotification_Spec extends Specification {

    public static final String ANDROID_ICON_RESOURCE_NAME = 'some_id_of_icon_resource_on_android'
    public static final int PUSH_TOKENS_LIMIT = 300

    DummyDatabaseIds dummyDatabaseIds = new DummyDatabaseIds()

    NotificationsSenderService notificationsSenderService = Mock(NotificationsSenderService)
    BuildRepository buildRepository = Mock(BuildRepository)
    PushTokenRepository pushTokenRepository = Mock(PushTokenRepository)

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
                Mock(UserService),
                Mock(PermissionChecks),
                Mock(ProjectService),
                buildService,
                ANDROID_ICON_RESOURCE_NAME,
                PUSH_TOKENS_LIMIT
        )
        project = newProject('Some Project')
        def releaser = newUser('releaser@shuttle.com', 'Build Releaser')
        platform = Platform.IOS
        anotherPlatform = Platform.ANDROID
        appId = 'some.app.id'
        app = newApp(project, platform, appId, 'Some App', 'http://app.icon.href')
        buildIdentifier = 'some.build.identifier'
        build = newBuild(buildIdentifier, '1.2.3', app, releaser)
    }

    def "throw exception if there is no Build"() {
        given:
        buildRepository.find(platform, appId, buildIdentifier) >> Optional.empty()

        when:
        notificationService.notifyAboutPublishedBuild(platform, appId, buildIdentifier)

        then:
        thrown(BuildNotFoundException)

        and:
        0 * notificationsSenderService.send(_)
    }

    def "do not notify if there are no Assignees to notify"() {
        given:
        buildRepository.find(platform, appId, buildIdentifier) >> Optional.of(build)

        when:
        notificationService.notifyAboutPublishedBuild(platform, appId, buildIdentifier)

        then:
        0 * notificationsSenderService.send(_)
    }

    def "do not notify if Assignees have no Push Tokens"() {
        given:
        def assignee1 = newUser('assignee1@shuttle.com', '1st Project Assignee')
        def assignee2 = newUser('assignee2@shuttle.com', '2nd Project Assignee')
        project.assign(assignee1)
        project.assign(assignee2)

        and:
        buildRepository.find(platform, appId, buildIdentifier) >> Optional.of(build)

        and:
        pushTokenRepository.findTokensOwnedBy(assignee1) >> []
        pushTokenRepository.findTokensOwnedBy(assignee2) >> []

        when:
        notificationService.notifyAboutPublishedBuild(platform, appId, buildIdentifier)

        then:
        0 * notificationsSenderService.send(_)
    }

    def "notify Assignees using all their Push Tokens"() {
        given:
        def assignee1 = newUser('assignee1@shuttle.com', '1st Project Assignee')
        def assignee2 = newUser('assignee2@shuttle.com', '2nd Project Assignee')
        def assignee3 = newUser('assignee2@shuttle.com', '3rd Project Assignee')

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
        notificationService.notifyAboutPublishedBuild(platform, appId, buildIdentifier)

        then:
        1 * notificationsSenderService.send(_) >> { arguments -> sentNotificationJson = arguments[0] as FirebaseNotification }
        sentNotificationJson.registrationIds == ['a1pt1', 'a1pt2', 'a3pt1'] as Set
        sentNotificationJson.notificationDetails.bodyKey == 'notification_about_published_build_body'
        sentNotificationJson.notificationDetails.bodyArguments == [build.versionNumber(), app.name()]
        sentNotificationJson.notificationDetails.androidIconResourceName == ANDROID_ICON_RESOURCE_NAME
        sentNotificationJson.customData.notificationType == 'PublishedBuild'
        sentNotificationJson.customData.projectId == project.id
        sentNotificationJson.customData.appId == appId
        sentNotificationJson.customData.appName == app.name()
        sentNotificationJson.customData.appIconHref == app.iconHref()
    }

    def "notify Assignees by partitioning their Push Tokens"() {
        given:
        def numberOfAllPushTokens = 3000
        def assignee1 = newUser('assignee1@shuttle.com', '1st Project Assignee')
        def assignee2 = newUser('assignee2@shuttle.com', '2nd Project Assignee')
        def assignee3 = newUser('assignee2@shuttle.com', '3rd Project Assignee')

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
        notificationService.notifyAboutPublishedBuild(platform, appId, buildIdentifier)

        then:
        (numberOfAllPushTokens/PUSH_TOKENS_LIMIT) * notificationsSenderService.send(_) >> _
    }

    def "notify only Assignees who have not muted an App of published Build"() {
        given:
        def assignee1 = newUser('assignee1@shuttle.com', '1st Project Assignee')
        def assignee2 = newUser('assignee2@shuttle.com', '2nd Project Assignee')

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
        notificationService.notifyAboutPublishedBuild(platform, appId, buildIdentifier)

        then:
        1 * notificationsSenderService.send(_) >> { arguments -> sentNotificationJson = arguments[0] as FirebaseNotification }
        sentNotificationJson.registrationIds == ['a2pt'] as Set
    }

    def "notify only Assignees who have not muted a Project of published Build"() {
        given:
        def assignee1 = newUser('assignee1@shuttle.com', '1st Project Assignee')
        def assignee2 = newUser('assignee2@shuttle.com', '2nd Project Assignee')

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
        notificationService.notifyAboutPublishedBuild(platform, appId, buildIdentifier)

        then:
        1 * notificationsSenderService.send(_) >> { arguments -> sentNotificationJson = arguments[0] as FirebaseNotification }
        sentNotificationJson.registrationIds == ['a2pt'] as Set
    }

    def "notify only Assignees who have not archived a Project of published Build"() {
        given:
        def assignee1 = newUser('assignee1@shuttle.com', '1st Project Assignee')
        def assignee2 = newUser('assignee2@shuttle.com', '2nd Project Assignee')

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
        notificationService.notifyAboutPublishedBuild(platform, appId, buildIdentifier)

        then:
        1 * notificationsSenderService.send(_) >> { arguments -> sentNotificationJson = arguments[0] as FirebaseNotification }
        sentNotificationJson.registrationIds == ['a2pt'] as Set
    }

    def "notify Assignees using only Push Tokens for Platform of published Build"() {
        given:
        def assignee = newUser('assignee@shuttle.com', 'Project Assignee')
        project.assign(assignee)

        and:
        buildRepository.find(platform, appId, buildIdentifier) >> Optional.of(build)

        and:
        pushTokenRepository.findTokensOwnedBy(assignee) >> [
                newPushToken(assignee, 'ptOfMatchingPlatform', platform),
                newPushToken(assignee, 'ptOfAnotherPlatform', anotherPlatform)
        ]

        when:
        notificationService.notifyAboutPublishedBuild(platform, appId, buildIdentifier)

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
