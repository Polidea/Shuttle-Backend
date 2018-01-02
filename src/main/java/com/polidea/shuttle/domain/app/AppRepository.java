package com.polidea.shuttle.domain.app;

import com.polidea.shuttle.domain.project.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Repository
public class AppRepository {

    private final AppJpaRepository jpaRepository;

    @Autowired
    public AppRepository(AppJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    void createNewApp(Project project,
                      Platform platform,
                      String appId,
                      String name,
                      String iconHref) {
        App newApp = new App(project, platform, appId, name);
        newApp.setIconHref(iconHref);
        jpaRepository.save(newApp);
    }

    public Optional<App> find(Platform platform, String appId) {
        return jpaRepository.findByAppIdInAndPlatform(appId, platform)
                            .stream()
                            .filter(app -> !app.isDeleted())
                            .findAny();
    }

    Set<App> find(Integer projectId, Platform platform) {
        return jpaRepository.findNonDeletedByProjectIdAndPlatform(projectId, platform)
                            .stream()
                            .collect(toSet());
    }

    void delete(App appToDelete) {
        appToDelete.delete();
    }
}
