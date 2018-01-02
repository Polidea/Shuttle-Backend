package com.polidea.shuttle.domain.user.permissions.project;

import com.polidea.shuttle.domain.project.Project;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.permissions.PermissionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Repository
public class ProjectPermissionRepository {

    private final ProjectPermissionJpaRepository jpaRepository;

    @Autowired
    public ProjectPermissionRepository(ProjectPermissionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    void createProjectPermission(User user, Project project, PermissionType permissionTypeType) {
        jpaRepository.save(new ProjectPermission(user, project, permissionTypeType));
    }

    public Set<ProjectPermission> findByUser(User user) {
        return jpaRepository.findByUser(user);
    }

    public Set<ProjectPermission> findByUserAndProject(User user, Project project) {
        return jpaRepository.findByUserAndProject(user, project)
                            .stream()
                            .collect(toSet());
    }

    public List<ProjectPermission> deleteByUserEmailAndProjectId(User user, Project project) {
        return jpaRepository.deleteByUserAndProject(user, project);
    }

    Optional<ProjectPermission> findByUserAndProjectAndPermission(User user, Project project, PermissionType permissionType) {
        return jpaRepository.findByUserAndProjectAndType(user, project, permissionType);
    }

    public void delete(ProjectPermission projectPermission) {
        jpaRepository.delete(projectPermission);
    }

    public Set<ProjectPermission> findAll() {
        return jpaRepository.findAll()
                            .stream()
                            .collect(toSet());
    }
}
