package com.polidea.shuttle.domain.user.permissions.global;

import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.permissions.PermissionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Repository
public class GlobalPermissionRepository {

    private final GlobalPermissionJpaRepository jpaRepository;

    @Autowired
    GlobalPermissionRepository(GlobalPermissionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    void createGlobalPermissions(User user, List<PermissionType> permissionTypeTypes) {
        List<GlobalPermission> newGlobalPermissions =
            permissionTypeTypes.stream()
                  .map(permission -> new GlobalPermission(user, permission))
                  .collect(toList());
        jpaRepository.save(newGlobalPermissions);

    }

    public Set<GlobalPermission> findFor(User user) {
        return new HashSet<>(jpaRepository.findByUser(user));
    }

    public Set<GlobalPermission> findAllOfGlobalAdminAccessType() {
        return jpaRepository.findByTypeAndUser_isDeletedFalse(PermissionType.ADMIN);
    }

    public void delete(User user) {
        jpaRepository.deleteByUser(user);
    }
}
