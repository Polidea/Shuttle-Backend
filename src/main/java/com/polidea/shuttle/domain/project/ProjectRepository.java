package com.polidea.shuttle.domain.project;

import com.polidea.shuttle.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Repository
public class ProjectRepository {

    private final ProjectJpaRepository jpaRepository;

    @Autowired
    ProjectRepository(ProjectJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    Project createNewProject(String name, String iconHref) {
        Project newProject = new Project(name);
        newProject.setIconHref(iconHref);
        return jpaRepository.save(newProject);
    }

    public Optional<Project> findById(Integer projectId) {
        return jpaRepository.findById(projectId)
                            .stream()
                            .filter(project -> !project.isDeleted())
                            .findFirst();
    }

    Optional<Project> findByName(String name) {
        return jpaRepository.findByName(name)
                            .stream()
                            .filter(project -> !project.isDeleted())
                            .findFirst();
    }

    public Set<Project> findAll() {
        return jpaRepository.findAll()
                            .stream()
                            .filter(project -> !project.isDeleted())
                            .collect(toSet());
    }

    void delete(Project projectToDelete) {
        projectToDelete.delete();
    }

    public Set<Project> projectsOfAssignee(User assignee) {
        return jpaRepository.findNonDeletedByAssignee(assignee.id())
                            .stream()
                            .collect(toSet());
    }
}
