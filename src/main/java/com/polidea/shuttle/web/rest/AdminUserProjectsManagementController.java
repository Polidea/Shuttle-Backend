package com.polidea.shuttle.web.rest;

import com.polidea.shuttle.domain.notifications.NotificationService;
import com.polidea.shuttle.domain.project.ProjectService;
import com.polidea.shuttle.domain.user.UserProjectService;
import com.polidea.shuttle.domain.user.output.AdminProjectAssigneeListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@SuppressWarnings("unused")
@RestController
public class AdminUserProjectsManagementController {

    private final UserProjectService userProjectService;
    private final ProjectService projectService;
    private final NotificationService notificationService;

    @Autowired
    public AdminUserProjectsManagementController(UserProjectService userProjectService,
                                                 ProjectService projectService,
                                                 NotificationService notificationService) {
        this.userProjectService = userProjectService;
        this.projectService = projectService;
        this.notificationService = notificationService;
    }

    @RequestMapping(method = GET, value = "/admin/projects/{projectId:.+}/users")
    @PreAuthorize("@permissionChecks.check(principal).canAdminister().or().canModerate(#projectId).execute()")
    @ResponseStatus(OK)
    public AdminProjectAssigneeListResponse getProjectUsers(@PathVariable Integer projectId) {
        return projectService.fetchAssignedUsers(projectId);
    }

    @RequestMapping(value = "/admin/projects/{projectId:.+}/users/{assigneeEmail:.+}", method = POST)
    @PreAuthorize("@permissionChecks.check(principal).canAdminister().or().canModerate(#projectId).execute()")
    @ResponseStatus(NO_CONTENT)
    public void assignProjectToUser(@PathVariable String assigneeEmail, @PathVariable Integer projectId) {
        userProjectService.assignUserToProject(assigneeEmail, projectId);
        notificationService.notifyAboutProjectAssignment(assigneeEmail, projectId);
    }

    @RequestMapping(value = "/admin/projects/{projectId:.+}/users/{assigneeEmail:.+}", method = RequestMethod.DELETE)
    @PreAuthorize("@permissionChecks.check(principal).canAdminister().or().canModerate(#projectId).execute()")
    @ResponseStatus(NO_CONTENT)
    public void unassignProjectFromUser(@PathVariable String assigneeEmail,
                                        @PathVariable Integer projectId) {
        userProjectService.unassignUserFromProject(assigneeEmail, projectId);
    }
}
