package com.polidea.shuttle.domain.user.permissions.global;


import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.permissions.PermissionType;
import com.polidea.shuttle.infrastructure.database.BaseEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;

@Entity(name = "global_permissions")
@Table(name = "global_permissions")
public class GlobalPermission extends BaseEntity {

    @ManyToOne(fetch = LAZY,
               cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "type")
    @NotNull
    @Enumerated(value = STRING)
    private PermissionType type;

    // constructor without parameters is required by Hibernate
    @SuppressWarnings("unused")
    public GlobalPermission() {
    }

    public GlobalPermission(User user, PermissionType type) {
        this.user = user;
        this.type = type;
    }

    public boolean isOfType(PermissionType permissionTypeToCheck) {
        return type.equals(permissionTypeToCheck);
    }

    public PermissionType type() {
        return type;
    }

    public User user() {
        return user;
    }

}
