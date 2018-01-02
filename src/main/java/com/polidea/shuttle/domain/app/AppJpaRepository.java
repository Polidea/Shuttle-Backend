package com.polidea.shuttle.domain.app;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface AppJpaRepository extends JpaRepository<App, Integer> {

    List<App> findByAppIdInAndPlatform(String appId, Platform platform);

    @Query("   SELECT a FROM apps a " +
               "WHERE a.project.id = :projectId " +
               "  AND a.platform = :platform " +
               "  AND a.isDeleted = false")
    List<App> findNonDeletedByProjectIdAndPlatform(@Param("projectId") Integer pId, @Param("platform") Platform platform);

}
