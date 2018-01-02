package com.polidea.shuttle.data;

import com.google.common.collect.Lists;
import com.polidea.shuttle.domain.app.AppNotFoundException;
import com.polidea.shuttle.domain.app.AppService;
import com.polidea.shuttle.domain.app.DeploymentService;
import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.app.input.AppAdditionRequest;
import com.polidea.shuttle.domain.app.input.AppEditionRequest;
import com.polidea.shuttle.domain.app.input.BuildRequest;
import com.polidea.shuttle.domain.app.input.DeploymentMetadataRequest;
import com.polidea.shuttle.domain.app.input.ProjectAdditionRequest;
import com.polidea.shuttle.domain.app.input.ProjectEditionRequest;
import com.polidea.shuttle.domain.build.BuildNotFoundException;
import com.polidea.shuttle.domain.build.BuildService;
import com.polidea.shuttle.domain.project.Project;
import com.polidea.shuttle.domain.project.ProjectNotFoundException;
import com.polidea.shuttle.domain.project.ProjectService;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.UserNotFoundException;
import com.polidea.shuttle.domain.user.UserProjectService;
import com.polidea.shuttle.domain.user.UserService;
import com.polidea.shuttle.domain.user.input.EditUserRequest;
import com.polidea.shuttle.domain.user.input.NewUserRequest;
import com.polidea.shuttle.domain.user.permissions.PermissionType;
import com.polidea.shuttle.domain.user.permissions.global.GlobalPermissionsService;
import com.polidea.shuttle.domain.user.permissions.global.input.PermissionsAssignmentRequest;
import com.polidea.shuttle.domain.user.permissions.project.ProjectPermissionService;
import com.polidea.shuttle.domain.user.access_token.AccessTokenService;
import com.polidea.shuttle.domain.user.access_token.TokenType;
import com.polidea.shuttle.domain.verification_code.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataLoadHelper {

    private final UserService userService;
    private final AccessTokenService accessTokenService;
    private final GlobalPermissionsService globalPermissionsService;
    private final ProjectPermissionService projectPermissionService;
    private final ProjectService projectService;
    private final AppService appService;
    private final BuildService buildService;
    private final DeploymentService deploymentService;
    private final UserProjectService userProjectService;
    private final VerificationCodeService verificationCodeService;

    @Autowired
    public DataLoadHelper(UserService userService,
                          AccessTokenService accessTokenService,
                          GlobalPermissionsService globalPermissionsService,
                          ProjectPermissionService projectPermissionService,
                          ProjectService projectService,
                          AppService appService,
                          BuildService buildService,
                          DeploymentService deploymentService,
                          UserProjectService userProjectService,
                          VerificationCodeService verificationCodeService) {
        this.userService = userService;
        this.accessTokenService = accessTokenService;
        this.globalPermissionsService = globalPermissionsService;
        this.projectPermissionService = projectPermissionService;
        this.projectService = projectService;
        this.appService = appService;
        this.buildService = buildService;
        this.deploymentService = deploymentService;
        this.userProjectService = userProjectService;
        this.verificationCodeService = verificationCodeService;
    }

    public void createUserIfMissing(String email, String name) {
        createUserIfMissing(email, name, null);
    }

    public void createUserIfMissing(String email, String name, String avatarHref) {
        try {
            EditUserRequest editUserRequest = new EditUserRequest();
            editUserRequest.setName(name);
            if (avatarHref != null) {
                editUserRequest.setAvatarHref(avatarHref);
            }
            userService.editUser(email, editUserRequest);
        } catch (UserNotFoundException exception) {
            NewUserRequest newUserRequest = new NewUserRequest();
            newUserRequest.email = email;
            newUserRequest.name = name;
            newUserRequest.avatarHref = avatarHref;
            userService.addNewUser(newUserRequest);
        }
    }

    public void deleteUser(String email) {
        userService.deleteUser(email);
    }

    public void setVerificationCode(String deviceId, String email, String verificationCode) {
        verificationCodeService.createOrUpdateVerificationCodeWithValue(deviceId, email, verificationCode);
    }

    public void createOrRenewAccessToken(String ownerEmail, TokenType tokenType, String tokenValue, String deviceId) {
        User owner = userService.findUser(ownerEmail);
        accessTokenService.deleteBy(owner, deviceId, tokenType);
        accessTokenService.saveAccessToken(
            owner,
            deviceId,
            tokenType,
            tokenValue,
            Instant.now()
        );
    }

    public void setGlobalPermissions(String userEmail, PermissionType... permissionTypes) {
        setGlobalPermissions(userEmail, Lists.newArrayList(permissionTypes));
    }

    public void setGlobalPermissions(String userEmail, List<PermissionType> permissionTypes) {
        PermissionsAssignmentRequest permissionsAssignmentRequest = new PermissionsAssignmentRequest();
        permissionsAssignmentRequest.permissions = permissionTypes;
        globalPermissionsService.assignPermissions(permissionsAssignmentRequest, userEmail);
    }

    public void setProjectPermissions(String userEmail, int projectId, ArrayList<PermissionType> permissionTypes) {
        for (PermissionType permissionType : PermissionType.values()) {
            projectPermissionService.unassignProjectPermission(permissionType, userEmail, projectId);
        }
        for (PermissionType permissionType : permissionTypes) {
            projectPermissionService.assignProjectPermission(permissionType, userEmail, projectId);
        }
    }

    public int createProjectIfMissing(String name) {
        return createProjectIfMissing(name, null);
    }

    int createProjectIfMissing(String name, String iconHref) {
        Project project;
        try {
            project = projectService.findByName(name);
            ProjectEditionRequest projectEditionRequest = new ProjectEditionRequest();
            projectEditionRequest.setName(name);
            if (iconHref != null) {
                projectEditionRequest.setIconHref(iconHref);
            }
            projectService.editProject(project.id(), projectEditionRequest);
        } catch (ProjectNotFoundException exception) {
            ProjectAdditionRequest projectAdditionRequest = new ProjectAdditionRequest();
            projectAdditionRequest.name = name;
            projectAdditionRequest.iconHref = iconHref;
            project = projectService.addProject(projectAdditionRequest);
        }
        return project.id();
    }

    public void assignUserToProject(String userEmail, int project1Id) {
        userProjectService.assignUserToProject(userEmail, project1Id);
    }

    public void addMemberToProject(String userEmail, int projectId) {
        projectService.addMember(projectId, userEmail);
    }

    public void createAppIfMissing(int projectId, Platform platform, String appId, String name) {
        createAppIfMissing(projectId, platform, appId, name, null);
    }

    void createAppIfMissing(int projectId, Platform platform, String appId, String name, String iconHref) {
        try {
            AppEditionRequest appEditionRequest = new AppEditionRequest();
            appEditionRequest.setName(name);
            if (iconHref != null) {
                appEditionRequest.setIconHref(iconHref);
            }
            appService.editApp(appEditionRequest, platform, appId);
        } catch (AppNotFoundException exception) {
            AppAdditionRequest appAdditionRequest = new AppAdditionRequest();
            appAdditionRequest.name = name;
            appAdditionRequest.iconHref = iconHref;
            appService.addApp(appAdditionRequest, projectId, platform, appId);
        }
    }

    public void createBuildIfMissing(Platform platform,
                                     String appId,
                                     String buildIdentifier,
                                     String versionNumber,
                                     Long bytes,
                                     String href,
                                     String releaserEmail) {

        try {
            buildService.findBuild(platform, appId, buildIdentifier);
        } catch (BuildNotFoundException exception) {
            BuildRequest buildRequest = new BuildRequest();
            buildRequest.setBuildIdentifier(buildIdentifier);
            buildRequest.setVersion(versionNumber);
            if (bytes != null) {
                buildRequest.setBytes(bytes);
            }
            buildRequest.setHref(href);
            buildRequest.setReleaseNotes(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\n" +
                    "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
                    "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.\n" +
                    "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            );
            buildRequest.setReleaserEmail(releaserEmail);
            DeploymentMetadataRequest deploymentMetadataRequest = new DeploymentMetadataRequest();
            deploymentMetadataRequest.build = buildRequest;
            deploymentService.registerNewBuild(deploymentMetadataRequest, appId, platform);
        }
    }

    public void publishBuild(Platform platform, String appId, String buildIdentifier) {
        buildService.publishBuild(platform, appId, buildIdentifier);
    }

    public void archiveProjectByUser(String userEmail, int projectId) {
        projectService.archive(projectId, userEmail);
    }

    public void favoriteBuildByUser(String userEmail, Platform platform, String appId, String buildIdentifier) {
        buildService.favoriteBuild(userEmail, platform, appId, buildIdentifier);
    }
}
