package com.polidea.shuttle.web.rest;

import com.polidea.shuttle.domain.app.App;
import com.polidea.shuttle.domain.app.AppNotFoundException;
import com.polidea.shuttle.domain.app.AppNotFoundInProjectException;
import com.polidea.shuttle.domain.app.AppRepository;
import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.build.Build;
import com.polidea.shuttle.domain.build.BuildNotFoundException;
import com.polidea.shuttle.domain.build.BuildRepository;
import com.polidea.shuttle.domain.project.ProjectNotFoundException;
import com.polidea.shuttle.domain.project.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class BuildPathValidation {

    private final ProjectRepository projectRepository;
    private final AppRepository appRepository;
    private final BuildRepository buildRepository;

    @Autowired
    public BuildPathValidation(ProjectRepository projectRepository, AppRepository appRepository, BuildRepository buildRepository) {
        this.projectRepository = projectRepository;
        this.appRepository = appRepository;
        this.buildRepository = buildRepository;
    }

    void assertValidPath(Integer projectId, Platform platform, String appId, String buildIdentifier) {
        assertThatProjectExists(projectId);
        assertThatAppExists(projectId, platform, appId);
        assertThatBuildExists(platform, appId, buildIdentifier);
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

    private Build assertThatBuildExists(Platform platform, String appId, String buildIdentifier) {
        return buildRepository.find(platform, appId, buildIdentifier)
                              .orElseThrow(() -> new BuildNotFoundException(buildIdentifier));
    }
}
