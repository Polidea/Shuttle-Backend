package com.polidea.shuttle.domain.user.permissions.project;

import com.polidea.shuttle.domain.project.Project;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.permissions.PermissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
interface ProjectPermissionJpaRepository extends JpaRepository<ProjectPermission, Integer> {

    Set<ProjectPermission> findByUser(User user);

    List<ProjectPermission> findByUserAndProject(User user, Project project);

    Optional<ProjectPermission> findByUserAndProjectAndType(User user, Project project, PermissionType type);

    @Modifying
    List<ProjectPermission> deleteByUserAndProject(User user, Project project);
}
