package com.polidea.shuttle.web.rest;


import com.polidea.shuttle.domain.user.permissions.global.GlobalPermissionsService;
import com.polidea.shuttle.domain.user.permissions.global.input.PermissionsAssignmentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/admin/users")
public class AdminGlobalPermissionsController {

    private final GlobalPermissionsService globalPermissionsService;

    @Autowired
    public AdminGlobalPermissionsController(GlobalPermissionsService globalPermissionsService) {
        this.globalPermissionsService = globalPermissionsService;
    }

    @RequestMapping(value = "/{assigneeEmail}/permissions", method = POST)
    @PreAuthorize("@permissionChecks.check(principal).canAdminister().execute()")
    @ResponseStatus(NO_CONTENT)
    public void assignPermissions(@RequestBody @Valid PermissionsAssignmentRequest permissionsAssignmentRequest,
                                  @PathVariable String assigneeEmail) {
        globalPermissionsService.assignPermissions(permissionsAssignmentRequest, assigneeEmail);
    }

}
