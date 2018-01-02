package com.polidea.shuttle.domain.user;


import com.polidea.shuttle.domain.project.Project;
import com.polidea.shuttle.domain.project.ProjectService;
import com.polidea.shuttle.domain.user.permissions.project.ProjectPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserProjectService {

    private final UserService userService;
    private final ProjectService projectService;
    private final ProjectPermissionService projectPermissionService;

    @Autowired
    public UserProjectService(UserService userService,
                              ProjectService projectService,
                              ProjectPermissionService projectPermissionService) {
        this.userService = userService;
        this.projectService = projectService;
        this.projectPermissionService = projectPermissionService;
    }

    public void assignUserToProject(String assigneeEmail, Integer projectId) {
        User user = userService.findUser(assigneeEmail);
        Project project = projectService.findProject(projectId);
        project.assign(user);
    }

    public void unassignUserFromProject(String assigneeEmail, Integer projectId) {
        projectPermissionService.unassignAllProjectPermissions(assigneeEmail, projectId);
        User user = userService.findUser(assigneeEmail);
        Project project = projectService.findProject(projectId);
        project.unassign(user);
    }

}
