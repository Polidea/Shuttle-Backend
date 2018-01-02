package com.polidea.shuttle.configuration;

import com.polidea.shuttle.domain.app.AppRepository;
import com.polidea.shuttle.domain.project.ProjectRepository;
import com.polidea.shuttle.domain.user.UserRepository;
import com.polidea.shuttle.domain.user.permissions.global.GlobalPermissionRepository;
import com.polidea.shuttle.domain.user.permissions.project.ProjectPermissionRepository;
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.context.annotation.RequestScope;

@Profile("!integrationTests")
@Configuration
@SuppressWarnings("unused")
public class PermissionChecksConfiguration {

    @Bean
    @RequestScope
    PermissionChecks permissionChecks(UserRepository userRepository,
                                      ProjectRepository projectRepository,
                                      AppRepository appRepository,
                                      GlobalPermissionRepository globalPermissionRepository,
                                      ProjectPermissionRepository projectPermissionRepository) {
        return new PermissionChecks(
            userRepository,
            projectRepository,
            appRepository,
            globalPermissionRepository,
            projectPermissionRepository
        );
    }
}
