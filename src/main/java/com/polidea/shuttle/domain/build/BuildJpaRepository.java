package com.polidea.shuttle.domain.build;

import com.polidea.shuttle.domain.app.Platform;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
interface BuildJpaRepository extends JpaRepository<Build, Integer> {

    List<Build> findByApp_appIdAndApp_platform(String appId, Platform platform);

    List<Build> findByIsPublishedAndApp_appIdAndApp_platform(Boolean isPublished, String appId, Platform platform);

    @Query("   SELECT b FROM builds b JOIN b.app a " +
               "WHERE a.appId = :appId " +
               "  AND a.platform = :platform " +
               "  AND b.isPublished = true " +
               "  AND b.isDeleted = false " +
               "ORDER BY b.creationTimestamp DESC ")
    List<Build> findNonDeletedPublishedFromNewestToOldest(@Param("appId") String appId, @Param("platform") Platform platform);

    @Query(value = "    SELECT * FROM builds as b " +
                   "INNER JOIN apps as a ON b.app_id = a.id " +
                   "     WHERE a.app_id = :appId " +
                   "       AND a.platform = :platform " +
                   "       AND b.is_deleted = false " +
                   "  ORDER BY b.creation_timestamp DESC " +
                   "   LIMIT 1 ", nativeQuery = true)
    Optional<Build> findNewestNonDeleted(@Param("appId") String appId, @Param("platform") String platform);

    @Query("   SELECT b FROM builds b " +
               " JOIN b.app a " +
               "WHERE a.appId = :appId " +
               "  AND a.platform = :platform " +
               "  AND b.buildIdentifier = :buildIdentifier " +
               "  AND b.isDeleted = false")
    Optional<Build> findNonDeleted(@Param("appId") String appId,
                                   @Param("platform") Platform platform,
                                   @Param("buildIdentifier") String buildIdentifier);
}
