package com.polidea.shuttle.domain.project.output.factories;

import com.polidea.shuttle.domain.project.Project;
import com.polidea.shuttle.domain.project.output.AdminProjectListResponse;
import com.polidea.shuttle.domain.project.output.AdminProjectResponse;
import com.polidea.shuttle.domain.project.output.ClientArchivedProjectListResponse;
import com.polidea.shuttle.domain.project.output.ClientArchivedProjectResponse;
import com.polidea.shuttle.domain.project.output.ClientProjectListWithLatestBuildsResponse;
import com.polidea.shuttle.domain.project.output.ClientProjectWithLatestBuildsResponse;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class ProjectListResponseFactory {

    private final ProjectResponseFactory projectResponseFactory;

    public ProjectListResponseFactory(PermissionChecks permissionChecks) {
        projectResponseFactory = new ProjectResponseFactory(permissionChecks);
    }

    public AdminProjectListResponse createAdminProjectListResponse(Set<Project> projects, User user) {
        List<AdminProjectResponse> projectResponses
            = projects.stream()
                      .map(project -> projectResponseFactory.createAdminProjectResponse(project, user))
                      .collect(toList());
        return new AdminProjectListResponse(projectResponses);
    }

    public ClientProjectListWithLatestBuildsResponse createClientProjectListWithLatestBuildsResponse(Set<Project> projects, User user) {
        List<ClientProjectWithLatestBuildsResponse> projectResponses =
            projects.stream()
                    .map(project -> projectResponseFactory.createClientProjectWithLatestBuildsResponse(project, user))
                    .collect(toList());
        return new ClientProjectListWithLatestBuildsResponse(projectResponses);
    }

    public ClientArchivedProjectListResponse createClientProjectListResponse(Set<Project> projects) {
        List<ClientArchivedProjectResponse> projectResponses =
            projects.stream()
                    .map(project -> projectResponseFactory.createClientArchivedProjectResponse(project))
                    .collect(toList());
        return new ClientArchivedProjectListResponse(projectResponses);
    }

}
