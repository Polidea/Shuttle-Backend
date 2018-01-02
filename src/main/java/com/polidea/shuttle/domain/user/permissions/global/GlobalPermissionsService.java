package com.polidea.shuttle.domain.user.permissions.global;

import com.polidea.shuttle.domain.user.User;
import com.polidea.shuttle.domain.user.UserNotFoundException;
import com.polidea.shuttle.domain.user.UserRepository;
import com.polidea.shuttle.domain.user.permissions.PermissionType;
import com.polidea.shuttle.domain.user.permissions.global.input.PermissionsAssignmentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class GlobalPermissionsService {

    private final GlobalPermissionRepository globalPermissionRepository;
    private final UserRepository userRepository;

    @Autowired
    public GlobalPermissionsService(GlobalPermissionRepository globalPermissionRepository, UserRepository userRepository) {
        this.globalPermissionRepository = globalPermissionRepository;
        this.userRepository = userRepository;
    }

    public void assignPermissions(PermissionsAssignmentRequest globalPermissionsAssignmentRequest,
                                  String assigneeEmail) {
        User user = userRepository.findUser(assigneeEmail)
                                  .orElseThrow(() -> new UserNotFoundException(assigneeEmail));

        assignPermissions(user, globalPermissionsAssignmentRequest.permissions);
    }

    public void assignPermissions(User user, List<PermissionType> permissions) {
        globalPermissionRepository.delete(user);
        globalPermissionRepository.createGlobalPermissions(user, permissions);
    }

    public Set<GlobalPermission> findFor(User user) {
        return globalPermissionRepository.findFor(user);
    }

    public Set<GlobalPermission> findAllGlobalAdministrators() {
        return globalPermissionRepository.findAllOfGlobalAdminAccessType();
    }
}
