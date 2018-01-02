package com.polidea.shuttle.domain.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectJpaRepository extends JpaRepository<Project, Integer> {

    List<Project> findByName(String name);

    List<Project> findById(Integer id);

    @Query("   SELECT p FROM projects p " +
               " JOIN p.rawAssignees u " +
               "WHERE u.id = (:assigneeId) " +
               "  AND p.isDeleted = false")
    List<Project> findNonDeletedByAssignee(@Param("assigneeId") Integer assigneeId);

}
