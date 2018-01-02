package com.polidea.shuttle.domain.user.permissions;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

public enum PermissionType {

    ADMIN("admin_access"),
    ARCHIVER("can_archive"),
    PUBLISHER("can_publish"),
    UNPUBLISHED_BUILDS_VIEWER("can_view_unpublished"),
    MUTER("can_mute"),
    BUILD_CREATOR("can_create_build");

    public final String name;

    PermissionType(String name) {
        this.name = name;
    }

    public static PermissionType determinePermissionType(String providedPermissionTypeName) {
        checkNotNull(providedPermissionTypeName, "'permission' must not be null");
        return Arrays.stream(values())
                     .filter(permission -> permission.name.equalsIgnoreCase(providedPermissionTypeName))
                     .findFirst()
                     .orElseThrow(() -> new InvalidPermissionTypeException(providedPermissionTypeName));
    }

}
