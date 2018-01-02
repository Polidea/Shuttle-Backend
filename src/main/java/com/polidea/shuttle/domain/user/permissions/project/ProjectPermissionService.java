package com.polidea.shuttle.domain.user.permissions.project;

import com.polidea.shuttle.domain.project.Project;
import com.polidea.shuttle.domain.project.ProjectNotFoundException;
import com.polidea.shuttle.domain.project.ProjectRepository;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.UserNotFoundException;
import com.polidea.shuttle.domain.user.UserRepository;
import com.polidea.shuttle.domain.user.permissions.PermissionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

import static com.google.common.collect.Sets.newHashSet;


@Service
@Transactional
public class ProjectPermissionService {

    private final ProjectPermissionRepository projectPermissionRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    @Autowired
    public ProjectPermissionService(UserRepository userRepository,
                                    ProjectRepository projectRepository,
                                    ProjectPermissionRepository projectPermissionRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.projectPermissionRepository = projectPermissionRepository;
    }

    public void assignProjectPermission(PermissionType permissionTypeType, String assigneeEmail, Integer projectId) {
        User user = findUser(assigneeEmail);
        Project project = findProject(projectId);

        Optional<ProjectPermission> projectPermission =
            projectPermissionRepository.findByUserAndProjectAndPermission(user, project, permissionTypeType);

        if (!projectPermission.isPresent()) {
            projectPermissionRepository.createProjectPermission(user, project, permissionTypeType);
        }
    }

    public void unassignProjectPermission(PermissionType permissionType, String assigneeEmail, Integer projectId) {
        User user = findUser(assigneeEmail);
        Project project = findProject(projectId);

        Optional<ProjectPermission> projectPermission =
            projectPermissionRepository.findByUserAndProjectAndPermission(user, project, permissionType);

        if (projectPermission.isPresent()) {
            projectPermissionRepository.delete(projectPermission.get());
        }
    }

    public void unassignAllProjectPermissions(String assigneeEmail, Integer projectId) {
        User user = findUser(assigneeEmail);
        Project project = findProject(projectId);

        projectPermissionRepository.deleteByUserEmailAndProjectId(user, project);
    }

    public Collection<ProjectPermission> findFor(User user, Project project) {
        return projectPermissionRepository.findByUserAndProject(user, project);
    }

    private User findUser(String userEmail) {
        return userRepository.findUser(userEmail)
                             .orElseThrow(() -> new UserNotFoundException(userEmail));
    }

    private Project findProject(Integer projectId) {
        return projectRepository.findById(projectId)
                                .orElseThrow(() -> new ProjectNotFoundException());
    }

}
