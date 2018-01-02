package com.polidea.shuttle.web.rest;

import com.polidea.shuttle.domain.app.App;
import com.polidea.shuttle.domain.app.AppNotFoundException;
import com.polidea.shuttle.domain.app.AppNotFoundInProjectException;
import com.polidea.shuttle.domain.app.AppRepository;
import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.project.ProjectNotFoundException;
import com.polidea.shuttle.domain.project.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class AppPathValidation {

    private final ProjectRepository projectRepository;
    private final AppRepository appRepository;

    @Autowired
    public AppPathValidation(ProjectRepository projectRepository, AppRepository appRepository) {
        this.projectRepository = projectRepository;
        this.appRepository = appRepository;
    }

    void assertValidPath(Integer projectId, Platform platform, String appId) {
        assertThatProjectExists(projectId);
        assertThatAppExists(projectId, platform, appId);
    }

    private void assertThatProjectExists(Integer projectId) {
        projectRepository.findById(projectId)
                         .orElseThrow(ProjectNotFoundException::new);
    }

    private void assertThatAppExists(Integer projectId, Platform platform, String appId) {
        App app = appRepository.find(platform, appId)
                               .orElseThrow(() -> new AppNotFoundException(platform, appId));
        if (!app.project().id().equals(projectId)) {
            throw new AppNotFoundInProjectException(platform, appId, projectId);
        }
    }

}

