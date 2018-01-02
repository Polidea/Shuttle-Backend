package com.polidea.shuttle.domain.project;

import com.polidea.shuttle.domain.app.input.ProjectAdditionRequest;
import com.polidea.shuttle.domain.app.input.ProjectEditionRequest;
import com.polidea.shuttle.domain.project.output.AdminProjectListResponse;
import com.polidea.shuttle.domain.project.output.ClientArchivedProjectListResponse;
import com.polidea.shuttle.domain.project.output.ClientProjectListWithLatestBuildsResponse;
import com.polidea.shuttle.domain.project.output.factories.ProjectListResponseFactory;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.UserNotFoundException;
import com.polidea.shuttle.domain.user.UserRepository;
import com.polidea.shuttle.domain.user.output.AdminProjectAssigneeListResponse;
import com.polidea.shuttle.domain.user.output.factories.UserListResponseFactory;
import com.polidea.shuttle.domain.user.permissions.global.GlobalPermissionsService;
import com.polidea.shuttle.domain.user.permissions.project.ProjectPermissionService;
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Service
@Transactional
public class ProjectService {

    private final UserListResponseFactory userListResponseFactory;
    private final ProjectListResponseFactory projectListResponseFactory;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository,
                          ProjectPermissionService projectPermissionService,
                          GlobalPermissionsService globalPermissionsService,
                          PermissionChecks permissionChecks) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.userListResponseFactory = new UserListResponseFactory(globalPermissionsService, projectPermissionService);
        this.projectListResponseFactory = new ProjectListResponseFactory(permissionChecks);
    }

    private void assertNotExists(String name) {
        Optional<Project> foundProject = projectRepository.findByName(name);
        if (foundProject.isPresent()) {
            throw new DuplicateProjectException(name);
        }
    }

    public Project addProject(ProjectAdditionRequest projectAdditionRequest) {
        assertNotExists(projectAdditionRequest.name);
        return projectRepository.createNewProject(
            projectAdditionRequest.name,
            projectAdditionRequest.iconHref
        );
    }

    public void editProject(Integer projectId, ProjectEditionRequest projectEditionRequest) {
        Project project = findProject(projectId);
        if (projectEditionRequest.name() != null) {
            project.setName(projectEditionRequest.name().value());
        }
        if (projectEditionRequest.iconHref() != null) {
            project.setIconHref(projectEditionRequest.iconHref().value());
        }
    }

    public void deleteProject(Integer projectId) {
        Project projectToDelete = findProject(projectId);
        projectRepository.delete(projectToDelete);
    }

    public void addMember(Integer projectId, String email) {
        Project project = findProject(projectId);
        User user = findUser(email);
        project.addMember(user);
    }

    public void removeMember(Integer projectId, String email) {
        Project project = findProject(projectId);
        User user = findUser(email);
        project.removeMember(user);
    }

    public void mute(Integer projectId, String userEmail) {
        User user = findUser(userEmail);
        Project project = findProject(projectId);
        user.mute(project);
    }

    public void unmute(Integer projectId, String userEmail) {
        User user = findUser(userEmail);
        Project project = findProject(projectId);
        user.unmute(project);
    }

    public void archive(Integer projectId, String userEmail) {
        User user = findUser(userEmail);
        Project project = findProject(projectId);
        if (project.hasAssigned(user)) {
            user.archive(project);
        } else {
            throw new ProjectNotFoundException();
        }
    }

    public void unarchive(Integer projectId, String userEmail) {
        User user = findUser(userEmail);
        Project project = findProject(projectId);
        if (project.hasAssigned(user)) {
            user.unarchive(project);
        } else {
            throw new ProjectNotFoundException();
        }
    }

    public void unarchiveAsAdmin(Integer projectId, String userEmail) {
        User user = findUser(userEmail);
        Project project = findProject(projectId);
        user.unarchive(project);
    }

    public AdminProjectAssigneeListResponse fetchAssignedUsers(Integer projectId) {
        Project project = findProject(projectId);
        return userListResponseFactory.createAdminProjectAssigneeListResponse(project);
    }

    public AdminProjectListResponse fetchAllUserProjectsForAdmin(String userEmail) {
        User user = findUser(userEmail);
        Set<Project> projects = projectRepository.projectsOfAssignee(user);
        return projectListResponseFactory.createAdminProjectListResponse(projects, user);
    }

    public ClientProjectListWithLatestBuildsResponse fetchAllUserProjectsWithLatestBuilds(String userEmail) {
        User user = findUser(userEmail);
        Set<Project> projects = projectRepository.projectsOfAssignee(user)
                                                 .stream()
                                                 .filter(assignedProject -> !user.hasArchived(assignedProject))
                                                 .collect(toSet());
        return projectListResponseFactory.createClientProjectListWithLatestBuildsResponse(projects, user);
    }

    public ClientArchivedProjectListResponse fetchAllArchivedUserProjects(String userEmail) {
        User user = findUser(userEmail);
        Set<Project> projects = projectRepository.projectsOfAssignee(user)
                                                 .stream()
                                                 .filter(assignedProject -> user.hasArchived(assignedProject))
                                                 .collect(toSet());
        return projectListResponseFactory.createClientProjectListResponse(projects);
    }

    public AdminProjectListResponse fetchAllProjects(String userEmail) {
        User user = findUser(userEmail);
        Set<Project> projects = projectRepository.findAll();
        return projectListResponseFactory.createAdminProjectListResponse(projects, user);
    }

    public Project findProject(Integer projectId) {
        return projectRepository.findById(projectId)
                                .orElseThrow(() -> new ProjectNotFoundException());
    }

    private User findUser(String email) {
        return userRepository.findUser(email)
                             .orElseThrow(() -> new UserNotFoundException(email));
    }

    public Project findByName(String name) {
        return projectRepository.findByName(name)
                                .orElseThrow(() -> new ProjectNotFoundException());
    }

}

