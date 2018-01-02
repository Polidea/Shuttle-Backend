package com.polidea.shuttle.domain.user.permissions.global;

import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.permissions.PermissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
interface GlobalPermissionJpaRepository extends JpaRepository<GlobalPermission, Integer> {

    List<GlobalPermission> findByUser(User user);

    Set<GlobalPermission> findByTypeAndUser_isDeletedFalse(PermissionType permissionType);

    @Modifying
    void deleteByUser(User user);

}
