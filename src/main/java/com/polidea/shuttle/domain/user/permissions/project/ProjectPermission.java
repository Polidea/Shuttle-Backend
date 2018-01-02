package com.polidea.shuttle.domain.user.permissions.project;


import com.polidea.shuttle.domain.project.Project;
import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.permissions.PermissionType;
import com.polidea.shuttle.infrastructure.database.BaseEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import static javax.persistence.FetchType.LAZY;

@Entity(name = "project_permissions")
public class ProjectPermission extends BaseEntity {

    @ManyToOne(fetch = LAZY,
               cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = LAZY,
               cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "type")
    @NotNull
    @Enumerated(value = EnumType.STRING)
    private PermissionType type;

    // constructor without parameters is required by Hibernate
    @SuppressWarnings("unused")
    public ProjectPermission() {
    }

    public ProjectPermission(User user, Project project, PermissionType type) {
        this.user = user;
        this.project = project;
        this.type = type;
    }

    public boolean isOfType(PermissionType permissionType) {
        return this.type.equals(permissionType);
    }

    public PermissionType type() {
        return type;
    }

    public User user() {
        return user;
    }

    public Project project() {
        return project;
    }

}
