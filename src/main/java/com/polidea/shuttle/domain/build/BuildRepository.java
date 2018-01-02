package com.polidea.shuttle.domain.build;

import com.polidea.shuttle.domain.app.App;
import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class BuildRepository {

    private final BuildJpaRepository buildJpaRepository;

    @Autowired
    public BuildRepository(BuildJpaRepository buildJpaRepository) {
        this.buildJpaRepository = buildJpaRepository;
    }

    void createBuild(String buildIdentifier,
                     String version,
                     String releaseNotes,
                     String href,
                     Long bytes,
                     App app,
                     User releaser,
                     String releaserEmail) {
        Build newBuild = new Build(
            buildIdentifier,
            version,
            releaseNotes,
            href,
            bytes,
            app,
            releaser,
            releaserEmail
        );
        buildJpaRepository.save(newBuild);
    }

    public Set<Build> find(Platform platform, String appId) {
        return buildJpaRepository.findByApp_appIdAndApp_platform(appId, platform)
                                 .stream()
                                 .filter(build -> !build.isDeleted())
                                 .collect(Collectors.toSet());
    }

    public Optional<Build> find(Platform platform, String appId, String buildIdentifier) {
        return buildJpaRepository.findNonDeleted(appId, platform, buildIdentifier);
    }

    public Set<Build> findPublished(Platform platform, String appId) {
        return buildJpaRepository.findByIsPublishedAndApp_appIdAndApp_platform(true, appId, platform)
                                 .stream()
                                 .filter(build -> !build.isDeleted())
                                 .collect(Collectors.toSet());
    }

    public Optional<Build> findNewestPublished(Platform platform, String appId) {
        return buildJpaRepository.findNonDeletedPublishedFromNewestToOldest(appId, platform)
                                 .stream()
                                 .findFirst();
    }

    public Optional<Build> findNewest(Platform platform, String appId) {
        return buildJpaRepository.findNewestNonDeleted(appId, platform.toString());
    }

    public void delete(Build buildToDelete) {
        buildToDelete.delete();
    }
}
