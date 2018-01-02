package com.polidea.shuttle.web.rest;

import com.polidea.shuttle.domain.project.ProjectService;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.UserService;
import com.polidea.shuttle.domain.user.input.EditUserRequest;
import com.polidea.shuttle.domain.user.input.NewUserRequest;
import com.polidea.shuttle.domain.user.output.AdminUserListResponse;
import com.polidea.shuttle.domain.user.output.AdminUserResponse;
import com.polidea.shuttle.error_codes.ForbiddenException;
import com.polidea.shuttle.infrastructure.mail.MailAuthService;
import com.polidea.shuttle.infrastructure.mail.UnableToGenerateQRCodeException;
import com.polidea.shuttle.infrastructure.security.authentication.AuthenticatedUser;
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/admin/users")
public class AdminUsersController {

    private final UserService userService;
    private final MailAuthService mailAuthService;
    private final ProjectService projectService;
    private final PermissionChecks permissionChecks;

    @Autowired
    public AdminUsersController(UserService userService,
                                MailAuthService mailAuthService,
                                ProjectService projectService,
                                PermissionChecks permissionChecks) {
        this.userService = userService;
        this.mailAuthService = mailAuthService;
        this.projectService = projectService;
        this.permissionChecks = permissionChecks;
    }

    @RequestMapping(method = GET)
    @PreAuthorize("@permissionChecks.check(principal).canAdminister().or().canModerateAtLeastOneProject().execute()")
    @ResponseStatus(OK)
    public AdminUserListResponse getAllUsers(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        if (checkIfModeratorAndNotGlobalAdmin(authenticatedUser)) {
            return userService.fetchAllVisibleForModeratorWithTheirProjects();
        } else {
            return userService.fetchAllWithTheirProjects();
        }
    }

    @RequestMapping(method = POST)
    @PreAuthorize("@permissionChecks.check(principal).canAdminister().or().canModerateAtLeastOneProject().execute()")
    @ResponseStatus(NO_CONTENT)
    public void addUser(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                        @RequestBody @Valid NewUserRequest newUserRequest) throws UnableToGenerateQRCodeException {
        User newUser = userService.addNewUser(newUserRequest);
        mailAuthService.sendInvitationEmail(newUser.email());
        userService.sendNotificationEmailsOfNewUser(authenticatedUser, newUser);
    }

    @RequestMapping(method = GET, value = "/{email:.+}")
    @PreAuthorize("@permissionChecks.check(principal).canAdminister().or().canModerateAtLeastOneProject().or().hasEmail(#email).execute()")
    @ResponseStatus(OK)
    public AdminUserResponse getUser(@PathVariable String email) {
        return userService.fetchUser(email);
    }

    @RequestMapping(method = PATCH, value = "/{email:.+}")
    @PreAuthorize("@permissionChecks.check(principal).canAdminister().or().hasEmail(#email).execute()")
    @ResponseStatus(NO_CONTENT)
    public void editUser(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                         @PathVariable String email,
                         @RequestBody @Valid EditUserRequest editUserRequest) {
        if (checkIfGlobalAdmin(authenticatedUser)) {
            userService.moderateUser(email, editUserRequest);
        } else {
            if (editUserRequest.isVisibleForModerator() != null) {
                throw new ForbiddenException("Only global administrator is able to modify 'isVisibleForModerator' property");
            }
            userService.editUser(email, editUserRequest);
        }
    }

    @RequestMapping(method = DELETE, value = "/{email:.+}")
    @PreAuthorize("@permissionChecks.check(principal).canAdminister().execute()")
    @ResponseStatus(NO_CONTENT)
    public void deleteUser(@PathVariable String email) {
        userService.deleteUser(email);
    }

    @RequestMapping(method = POST, value = "/{email:.+}/projects/{projectId}/unarchive")
    @PreAuthorize("@permissionChecks.check(principal).canAdminister().or().canModerate(#projectId).or().hasEmail(#email).execute()")
    @ResponseStatus(NO_CONTENT)
    public void unarchiveProjectForUser(@PathVariable String email, @PathVariable Integer projectId) {
        projectService.unarchiveAsAdmin(projectId, email);
    }

    private Boolean checkIfModeratorAndNotGlobalAdmin(AuthenticatedUser authenticatedUser) {
        return !permissionChecks.check(authenticatedUser).canAdminister().execute()
            && permissionChecks.check(authenticatedUser).canModerateAtLeastOneProject().execute();
    }

    private Boolean checkIfGlobalAdmin(AuthenticatedUser authenticatedUser) {
        return permissionChecks.check(authenticatedUser).canAdminister().execute();
    }
}
