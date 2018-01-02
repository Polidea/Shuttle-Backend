package com.polidea.shuttle.infrastructure

import com.polidea.shuttle.domain.app.App
import com.polidea.shuttle.domain.app.AppService
import com.polidea.shuttle.domain.app.DeploymentService
import com.polidea.shuttle.domain.app.Platform
import com.polidea.shuttle.domain.app.input.AppAdditionRequest
import com.polidea.shuttle.domain.app.input.AppEditionRequest
import com.polidea.shuttle.domain.app.input.BuildRequest
import com.polidea.shuttle.domain.app.input.DeploymentMetadataRequest
import com.polidea.shuttle.domain.app.input.ProjectAdditionRequest
import com.polidea.shuttle.domain.app.input.ProjectEditionRequest
import com.polidea.shuttle.domain.build.BuildService
import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.project.ProjectService
import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.UserProjectService
import com.polidea.shuttle.domain.user.UserService
import com.polidea.shuttle.domain.user.input.NewUserRequest
import com.polidea.shuttle.domain.user.permissions.PermissionType
import com.polidea.shuttle.domain.user.permissions.global.GlobalPermissionsService
import com.polidea.shuttle.domain.user.permissions.global.input.PermissionsAssignmentRequest
import com.polidea.shuttle.domain.user.permissions.project.ProjectPermissionService
import com.polidea.shuttle.domain.user.refresh_token.RefreshTokenService
import com.polidea.shuttle.domain.user.access_token.AccessTokenRepository
import com.polidea.shuttle.domain.user.access_token.AccessTokenService
import com.polidea.shuttle.domain.user.access_token.TokenType
import com.polidea.shuttle.domain.verification_code.VerificationCodeService
import com.polidea.shuttle.infrastructure.time.TimeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestComponent
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

import java.time.Instant

import static com.polidea.shuttle.test_config.TimeServiceConfigurationForTests.CURRENT_TIME_IN_TESTS
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric

@TestComponent
class SetupHelperForIntegrationTest {


    @Autowired
    private VerificationCodeService verificationCodeService
    @Autowired
    private UserService userService
    @Autowired
    private AccessTokenService tokenService
    @Autowired
    private RefreshTokenService refreshTokenService
    @Autowired
    private TimeService timeService
    @Autowired
    private GlobalPermissionsService globalPermissionsService
    @Autowired
    private ProjectPermissionService projectPermissionService
    @Autowired
    private ProjectService projectService
    @Autowired
    private AppService appService
    @Autowired
    private BuildService buildService
    @Autowired
    private DeploymentService deploymentService
    @Autowired
    private UserProjectService userProjectService
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder

    String createAndAuthenticateClientUser(String userEmail, String name, String avatarHref, String deviceId) {
        createUser(userEmail, name, avatarHref)
        return createClientAccessToken(userEmail, deviceId)
    }

    User createUser(String userEmail, String name, String avatarHref) {
        return userService.addNewUser(new NewUserRequest(
                email: userEmail,
                name: name,
                avatarHref: avatarHref
        ))
    }

    void deleteUser(String userEmail) {
        userService.deleteUser(userEmail)
    }

    void createAccessToken(String userEmail, String deviceId, String tokenValue) {
        User user = userService.findUser(userEmail)
        Instant creationTime = timeService.currentTime()
        tokenService.saveAccessToken(
                user,
                deviceId,
                TokenType.CLIENT,
                tokenValue,
                creationTime
        )
    }

    void createRefreshToken(String userEmail, String deviceId, String tokenValue) {
        User user = userService.findUser(userEmail)
        Instant creationTime = timeService.currentTime()
        refreshTokenService.saveRefreshToken(
                user,
                deviceId,
                tokenValue,
                creationTime
        )
    }

    void createVerificationCode(String verificationCodeValue, String deviceId, String userEmail) {
        verificationCodeService.createOrUpdateVerificationCode(
                deviceId,
                userEmail,
                bCryptPasswordEncoder.encode(verificationCodeValue)
        )
    }

    String createClientAccessToken(String tokenOwnerEmail, String deviceId) {
        def tokenOwner = userService.findUser(tokenOwnerEmail)
        def tokenValue = randomAlphanumeric(8)
        tokenService.saveAccessToken(
                tokenOwner,
                deviceId,
                TokenType.CLIENT,
                tokenValue,
                CURRENT_TIME_IN_TESTS
        )
        return tokenValue
    }

    void assignGlobalPermissions(String userEmail, List<PermissionType> permissions) {
        globalPermissionsService.assignPermissions(
                new PermissionsAssignmentRequest(permissions: permissions),
                userEmail
        )
    }

    void assignProjectPermission(Project project, String userEmail, PermissionType permission) {
        projectPermissionService.assignProjectPermission(
                permission,
                userEmail,
                project.id
        )
    }

    void assignProjectPermission(Integer projectId, String userEmail, PermissionType permission) {
        projectPermissionService.assignProjectPermission(
                permission,
                userEmail,
                projectId
        )
    }

    void unassignProjectPermission(Project project, String userEmail, PermissionType permission) {
        projectPermissionService.unassignProjectPermission(
                permission,
                userEmail,
                project.id
        )
    }

    Project createProject(String name, String iconHref) {
        return projectService.addProject(new ProjectAdditionRequest(name: name, iconHref: iconHref))
    }

    Project deleteProject(Integer projectId) {
        return projectService.deleteProject(projectId)
    }

    void editProject(int projectId, String newName, String newIconHref) {
        def editionRequest = new ProjectEditionRequest()
        editionRequest.setName(newName)
        editionRequest.setIconHref(newIconHref)
        projectService.editProject(projectId, editionRequest)
    }

    void addMemberToProject(Integer projectId, String newMemberEmail) {
        projectService.addMember(projectId, newMemberEmail)
    }

    void assignUserToProject(String userEmail, Project project) {
        assignUserToProject(userEmail, project.id)
    }

    void unassignUserFromProject(String userEmail, Project project) {
        unassignUserFromProject(userEmail, project.id)
    }

    void assignUserToProject(String newAssigneeEmail, int projectId) {
        userProjectService.assignUserToProject(newAssigneeEmail, projectId)
    }

    void unassignUserFromProject(String newAssigneeEmail, int projectId) {
        userProjectService.unassignUserFromProject(newAssigneeEmail, projectId)
    }

    void archiveProject(String userEmail, Integer projectId) {
        projectService.archive(projectId, userEmail)
    }

    App createApp(Project project, Platform platform, String appId, String appName, String iconHref) {
        appService.addApp(
                new AppAdditionRequest(name: appName, iconHref: iconHref),
                project.id,
                platform,
                appId
        )
        return appService.findApp(platform, appId)
    }

    void editApp(Platform platform, String appId, String newName, String newIconHref) {
        def editionRequest = new AppEditionRequest()
        editionRequest.setName(newName)
        editionRequest.setIconHref(newIconHref)
        appService.editApp(editionRequest, platform, appId)
    }

    void publishBuild(Platform platform, String appId, String buildIdentifier) {
        buildService.publishBuild(platform, appId, buildIdentifier);
    }

    void createBuild(String appId,
                     Platform platform,
                     String releaserEmail,
                     String buildIdentifier,
                     String version,
                     String releaseNotes,
                     String href,
                     long bytes) {
        def buildRequest = new BuildRequest(
                buildIdentifier,
                version,
                releaseNotes,
                href,
                bytes,
                releaserEmail
        )
        deploymentService.registerNewBuild(
                new DeploymentMetadataRequest(build: buildRequest),
                appId,
                platform
        )
    }
}
