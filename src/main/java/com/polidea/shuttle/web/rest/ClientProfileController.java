package com.polidea.shuttle.web.rest;

import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.UserService;
import com.polidea.shuttle.domain.user.input.ProfileUpdateRequest;
import com.polidea.shuttle.domain.user.output.ClientProfileResponse;
import com.polidea.shuttle.domain.user.output.UploadAvatarResponse;
import com.polidea.shuttle.infrastructure.external_storage.ExternalStorageUrl;
import com.polidea.shuttle.infrastructure.security.authentication.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/profile")
public class ClientProfileController {

    private final UserService userService;

    @Autowired
    public ClientProfileController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(method = GET)
    @ResponseStatus(OK)
    public ClientProfileResponse getProfile(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        User user = userService.findUser(authenticatedUser.userEmail);
        return new ClientProfileResponse(user);
    }

    @RequestMapping(method = PATCH)
    @ResponseStatus(NO_CONTENT)
    public void updateProfile(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                              @RequestBody @Valid ProfileUpdateRequest profileUpdateRequest) {
        userService.editUser(authenticatedUser.userEmail, profileUpdateRequest);
    }

    @RequestMapping(method = POST, path = "/avatar", consumes = MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(OK)
    public UploadAvatarResponse uploadAvatar(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                             @RequestParam("avatarImage") MultipartFile multipartAvatarImage) throws IOException, NoSuchAlgorithmException {
        ExternalStorageUrl avatarUrl = userService.uploadAvatar(authenticatedUser.userEmail, multipartAvatarImage);
        return new UploadAvatarResponse(avatarUrl.asText());
    }

}
