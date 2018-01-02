package com.polidea.shuttle.domain.notifications

import com.polidea.shuttle.domain.app.App
import com.polidea.shuttle.domain.app.Platform
import com.polidea.shuttle.domain.build.BuildService
import com.polidea.shuttle.domain.notifications.output.FirebaseNotification
import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.project.ProjectNotFoundException
import com.polidea.shuttle.domain.project.ProjectRepository
import com.polidea.shuttle.domain.project.ProjectService
import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.UserNotFoundException
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
import com.polidea.shuttle.test_config.DummyDatabaseIds
import spock.lang.Specification

class NotificationService_AssignmentToProjectNotification_Spec extends Specification {

    public static final String ANDROID_ICON_RESOURCE_NAME = 'some_id_of_icon_resource_on_android'
    public static final int PUSH_TOKENS_LIMIT = 300

    DummyDatabaseIds dummyDatabaseIds = new DummyDatabaseIds()

    NotificationsSenderService notificationsSenderService = Mock(NotificationsSenderService)
    UserRepository userRepository = Mock(UserRepository)
    ProjectRepository projectRepository = Mock(ProjectRepository)
    PushTokenRepository pushTokenRepository = Mock(PushTokenRepository)

    NotificationService notificationService

    Project project
    User assignee

    FirebaseNotification sentNotificationJson

    void setup() {
        def projectService = new ProjectService(
                projectRepository,
                userRepository,
                Mock(ProjectPermissionService),
                Mock(GlobalPermissionsService),
                Mock(PermissionChecks)
        )
        def userService = new UserService(
                userRepository,
                projectRepository,
                Mock(DefaultAvatars),
                Mock(GlobalPermissionsService),
                Mock(NewUserNotificationMailService),
                Mock(ProjectPermissionService),
                Mock(ExternalStorage),
                Mock(ExternalStoragePaths),
                new AvatarContentTypeFix()
        )
        def pushTokenService = new PushTokenService(
                pushTokenRepository,
                userService
        )
        notificationService = new NotificationService(
                notificationsSenderService,
                pushTokenService,
                userService,
                Mock(PermissionChecks),
                projectService,
                Mock(BuildService),
                ANDROID_ICON_RESOURCE_NAME,
                PUSH_TOKENS_LIMIT
        )
        project = newProject('Some Project')
        assignee = newUser('any.user@shuttle.com', 'Any User')
        project.assign(assignee)
        def app = newApp(project, Platform.ANDROID, 'some.app.id.1', 'Some App 1')
        project.rawApps = [app]
    }

    def "throw exception if there is no Assignee"() {
        given:
        userRepository.findUser(assignee.email()) >> Optional.empty()
        projectRepository.findById(project.id()) >> Optional.of(project)

        when:
        notificationService.notifyAboutProjectAssignment(assignee.email(), project.id)

        then:
        thrown(UserNotFoundException)

        and:
        0 * notificationsSenderService.send(_)
    }

    def "throw exception if there is no Project"() {
        given:
        userRepository.findUser(assignee.email()) >> Optional.of(assignee)
        projectRepository.findById(project.id()) >> Optional.empty()

        when:
        notificationService.notifyAboutProjectAssignment(assignee.email(), project.id)

        then:
        thrown(ProjectNotFoundException)

        and:
        0 * notificationsSenderService.send(_)
    }

    def "do not notify if Assignee has no Push Tokens"() {
        given:
        userRepository.findUser(assignee.email()) >> Optional.of(assignee)
        projectRepository.findById(project.id()) >> Optional.of(project)

        and:
        pushTokenRepository.findTokensOwnedBy(assignee) >> []

        when:
        notificationService.notifyAboutProjectAssignment(assignee.email(), project.id)

        then:
        0 * notificationsSenderService.send(_)
    }

    def "notify Assignee using all his Push Tokens"() {
        given:
        userRepository.findUser(assignee.email()) >> Optional.of(assignee)
        projectRepository.findById(project.id()) >> Optional.of(project)

        and:
        pushTokenRepository.findTokensOwnedBy(assignee) >> [
                newPushToken(assignee, 'pt1Android', Platform.ANDROID),
                newPushToken(assignee, 'pt2iOS', Platform.IOS),
                newPushToken(assignee, 'pt3iOS', Platform.IOS)
        ]

        when:
        notificationService.notifyAboutProjectAssignment(assignee.email(), project.id)

        then:
        1 * notificationsSenderService.send(_) >> { arguments -> sentNotificationJson = arguments[0] as FirebaseNotification }
        sentNotificationJson.registrationIds == ['pt1Android', 'pt2iOS', 'pt3iOS'] as Set
        sentNotificationJson.notificationDetails.bodyKey == 'notification_about_assignment_to_project_body'
        sentNotificationJson.notificationDetails.bodyArguments == [project.name()]
        sentNotificationJson.notificationDetails.androidIconResourceName == ANDROID_ICON_RESOURCE_NAME
        sentNotificationJson.customData.notificationType == 'AssignedToProject'
        sentNotificationJson.customData.projectId == project.id
        sentNotificationJson.customData.projectName == project.name()
    }

    def "notify Assignee by partitioning all his Push Tokens"() {
        given:
        def numberOfPushTokens = 1000
        userRepository.findUser(assignee.email()) >> Optional.of(assignee)
        projectRepository.findById(project.id()) >> Optional.of(project)

        and:
        pushTokenRepository.findTokensOwnedBy(assignee) >> (1..numberOfPushTokens).collect {
            newPushToken(assignee, it.toString(), Platform.ANDROID)
        }

        when:
        notificationService.notifyAboutProjectAssignment(assignee.email(), project.id)

        then:
        (numberOfPushTokens/PUSH_TOKENS_LIMIT + 1) * notificationsSenderService.send(_) >> { arguments -> sentNotificationJson = arguments[0] as FirebaseNotification }
    }

    def "do not notify Assignee if he muted a Project (eg. before unassignment and second assignment)"() {
        given:
        userRepository.findUser(assignee.email()) >> Optional.of(assignee)
        projectRepository.findById(project.id()) >> Optional.of(project)

        and:
        pushTokenRepository.findTokensOwnedBy(assignee) >> [newPushToken(assignee, 'pt', Platform.ANDROID)]

        and:
        assignee.mute(project)

        when:
        notificationService.notifyAboutProjectAssignment(assignee.email(), project.id)

        then:
        0 * notificationsSenderService.send(_)
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

    private App newApp(Project project, Platform platform, String appId, String name) {
        def app = new App(project, platform, appId, name)
        app.id = dummyDatabaseIds.next()
        project.rawApps.add(app)
        return app
    }

    private PushToken newPushToken(User owner, String value, Platform platform) {
        def pushToken = new PushToken(owner, platform, 'any-device-id', value)
        pushToken.id = dummyDatabaseIds.next()
        return pushToken
    }

}
