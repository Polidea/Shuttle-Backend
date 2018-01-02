package com.polidea.shuttle.domain.user;

import com.polidea.shuttle.domain.project.Project;
import com.polidea.shuttle.domain.project.ProjectRepository;
import com.polidea.shuttle.domain.user.input.EditUserRequest;
import com.polidea.shuttle.domain.user.input.NewUserRequest;
import com.polidea.shuttle.domain.user.input.ProfileUpdateRequest;
import com.polidea.shuttle.domain.user.output.AdminUserListResponse;
import com.polidea.shuttle.domain.user.output.AdminUserResponse;
import com.polidea.shuttle.domain.user.output.factories.UserListResponseFactory;
import com.polidea.shuttle.domain.user.output.factories.UserResponseFactory;
import com.polidea.shuttle.domain.user.permissions.global.GlobalPermission;
import com.polidea.shuttle.domain.user.permissions.global.GlobalPermissionsService;
import com.polidea.shuttle.domain.user.permissions.project.ProjectPermissionService;
import com.polidea.shuttle.error_codes.AvatarIsEmptyException;
import com.polidea.shuttle.error_codes.AvatarSizeTooBigException;
import com.polidea.shuttle.infrastructure.avatars.AvatarContentTypeFix;
import com.polidea.shuttle.infrastructure.avatars.DefaultAvatars;
import com.polidea.shuttle.infrastructure.external_storage.ExternalStorage;
import com.polidea.shuttle.infrastructure.external_storage.ExternalStoragePaths;
import com.polidea.shuttle.infrastructure.external_storage.ExternalStorageUrl;
import com.polidea.shuttle.infrastructure.external_storage.UploadToExternalStorageFailedException;
import com.polidea.shuttle.infrastructure.json.OptionalRequestField;
import com.polidea.shuttle.infrastructure.mail.NewUserNotificationMailService;
import com.polidea.shuttle.infrastructure.security.authentication.AuthenticatedUser;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static com.google.common.collect.Lists.newArrayList;
import static com.polidea.shuttle.domain.user.permissions.PermissionType.MUTER;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@Transactional
public class UserService {

    private static final Logger LOGGER = getLogger(UserService.class);
    private static final long MAX_ALLOWED_AVATAR_SIZE_IN_BYTES = 10 * 1024 * 1024;

    @Value("${shuttle.should-send-notification-of-new-user}")
    private boolean shouldSendNotificationOfNewUser;

    private final GlobalPermissionsService globalPermissionsService;
    private final NewUserNotificationMailService newUserNotificationMailService;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final DefaultAvatars defaultAvatars;
    private final UserResponseFactory userResponseFactory;
    private final UserListResponseFactory userListResponseFactory;
    private final ExternalStorage externalStorage;
    private final ExternalStoragePaths externalStoragePaths;
    private final AvatarContentTypeFix avatarContentTypeFix;

    @Autowired
    public UserService(UserRepository userRepository,
                       ProjectRepository projectRepository,
                       DefaultAvatars defaultAvatars,
                       GlobalPermissionsService globalPermissionsService,
                       NewUserNotificationMailService newUserNotificationMailService,
                       ProjectPermissionService projectPermissionService,
                       ExternalStorage externalStorage,
                       ExternalStoragePaths externalStoragePaths,
                       AvatarContentTypeFix avatarContentTypeFix) {
        this.globalPermissionsService = globalPermissionsService;
        this.newUserNotificationMailService = newUserNotificationMailService;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.defaultAvatars = defaultAvatars;
        this.externalStorage = externalStorage;
        this.externalStoragePaths = externalStoragePaths;
        this.avatarContentTypeFix = avatarContentTypeFix;
        this.userResponseFactory = new UserResponseFactory(globalPermissionsService, projectPermissionService);
        this.userListResponseFactory = new UserListResponseFactory(globalPermissionsService, projectPermissionService);
    }

    public User addNewUser(NewUserRequest newUserRequest) {
        LOGGER.info("Adding new user to store. User email: {}", newUserRequest.email);

        userRepository.findUser(newUserRequest.email).ifPresent(user -> {
            throw new UserAlreadyExistsException(user.email());
        });

        OptionalRequestField<Boolean> isVisibleForModerator = newUserRequest.isVisibleForModerator();

        User savedUser = userRepository.createUser(
            newUserRequest.email,
            newUserRequest.name,
            newUserRequest.avatarHref != null ? newUserRequest.avatarHref : defaultAvatars.random().url(),
            isVisibleForModerator == null ? false : isVisibleForModerator.value()
        );

        globalPermissionsService.assignPermissions(savedUser, newArrayList(MUTER));

        LOGGER.info("Successfully added new user: {}.", savedUser.email());
        return savedUser;
    }

    public void editUser(String userEmail, EditUserRequest editUserRequest) {
        LOGGER.info("Editing user {} with: {}", userEmail, editUserRequest);
        editUser(userEmail, editUserRequest.name(), editUserRequest.avatarHref());
    }

    public void editUser(String userEmail, ProfileUpdateRequest profileUpdateRequest) {
        LOGGER.info("Editing profile {} with: {}", userEmail, profileUpdateRequest);
        editUser(userEmail, profileUpdateRequest.name(), profileUpdateRequest.avatarHref());
    }

    private void editUser(String userEmail, OptionalRequestField<String> newName, OptionalRequestField<String> newAvatarHref) {
        User user = userRepository.findUser(userEmail)
                                  .orElseThrow(() -> new UserNotFoundException(userEmail));
        if (newName != null) {
            user.setName(newName.value());
        }
        if (newAvatarHref != null) {
            user.setAvatarHref(newAvatarHref.value());
        }
    }

    public void deleteUser(String email) {
        LOGGER.info("Deleting user {}", email);
        User userToDelete = findUser(email);
        userRepository.deleteUser(userToDelete);
    }

    public User findUser(String email) {
        return userRepository.findUser(email).orElseThrow(() -> new UserNotFoundException(email));
    }

    public User findUserWithoutException(String email) {
        return userRepository.findUser(email).orElse(null);
    }

    public AdminUserListResponse fetchAllWithTheirProjects() {
        Map<User, Set<Project>> usersAndTheirProjects = new HashMap<>();
        fetchAll().forEach(user -> {
            Set<Project> projects = projectRepository.projectsOfAssignee(user);
            usersAndTheirProjects.put(user, projects);
        });
        return userListResponseFactory.createAdminUserListResponse(usersAndTheirProjects);
    }

    public AdminUserListResponse fetchAllVisibleForModeratorWithTheirProjects() {
        Map<User, Set<Project>> usersAndTheirProjects = new HashMap<>();
        fetchAllVisibleForModerator().forEach(user -> {
            Set<Project> projects = projectRepository.projectsOfAssignee(user);
            usersAndTheirProjects.put(user, projects);
        });
        return userListResponseFactory.createAdminUserListResponse(usersAndTheirProjects);
    }

    public Set<User> fetchAll() {
        return userRepository.allUsers();
    }

    public AdminUserResponse fetchUser(String email) {
        User user = findUser(email);
        Set<Project> projects = projectRepository.projectsOfAssignee(user);
        return userResponseFactory.createAdminUserResponse(user, projects);
    }

    public ExternalStorageUrl uploadAvatar(String userEmail, MultipartFile multipartAvatarImage) {
        User user = findUser(userEmail);
        validateSizeOf(multipartAvatarImage);
        String avatarHash = hashOf(multipartAvatarImage);
        String resourcePathToSet = externalStoragePaths.pathForAvatarIdentifiedBy(avatarHash);
        String contentType = avatarContentTypeFix.fixed(multipartAvatarImage.getContentType());
        ExternalStorageUrl uploadedAvatarUrl = externalStorage.uploadFile(multipartAvatarImage, resourcePathToSet, contentType);
        user.setAvatarHref(uploadedAvatarUrl.asText());
        return uploadedAvatarUrl;
    }

    public void moderateUser(String userEmail, EditUserRequest editUserRequest) {
        User user = userRepository.findUser(userEmail)
                                  .orElseThrow(() -> new UserNotFoundException(userEmail));
        OptionalRequestField<Boolean> isVisibleForModerator = editUserRequest.isVisibleForModerator();
        if (isVisibleForModerator != null) {
            user.setVisibleForModerator(isVisibleForModerator.value());
        }

        editUser(userEmail, editUserRequest);
    }

    public void sendNotificationEmailsOfNewUser(AuthenticatedUser authenticatedUser,
                                                User newUser) {
        if (shouldSendNotificationOfNewUser) {
            globalPermissionsService.findAllGlobalAdministrators().stream()
                                    .forEach(
                                        sendNotificationEmailToGlobalAdministratorAboutNewUser(
                                            authenticatedUser,
                                            newUser
                                        )
                                    );
        }
    }

    private Set<User> fetchAllVisibleForModerator() {
        return userRepository.allUsersVisibleForModerator();
    }

    private Consumer<GlobalPermission> sendNotificationEmailToGlobalAdministratorAboutNewUser(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                                                              User newUser) {
        return globalAdministratorPermission -> {
            User globalAdmin = globalAdministratorPermission.user();
            newUserNotificationMailService.sendNotificationEmailAboutNewUser(
                globalAdmin.email(),
                authenticatedUser.userEmail,
                newUser.email(),
                newUser.name()
            );
        };
    }

    private void validateSizeOf(MultipartFile multipartAvatarImage) {
        if (multipartAvatarImage.getSize() > MAX_ALLOWED_AVATAR_SIZE_IN_BYTES) {
            throw new AvatarSizeTooBigException(
                multipartAvatarImage.getSize(),
                MAX_ALLOWED_AVATAR_SIZE_IN_BYTES
            );
        }
        if (multipartAvatarImage.getSize() == 0) {
            throw new AvatarIsEmptyException();
        }
    }

    private String hashOf(MultipartFile multipartAvatarImage) {
        try {
            return DigestUtils.md5Hex(multipartAvatarImage.getInputStream());
        } catch (Exception exception) {
            throw new UploadToExternalStorageFailedException();
        }
    }
}
