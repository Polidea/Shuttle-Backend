package com.polidea.shuttle.test_config

import com.polidea.shuttle.domain.app.AppRepository
import com.polidea.shuttle.domain.project.ProjectRepository
import com.polidea.shuttle.domain.user.UserRepository
import com.polidea.shuttle.domain.user.permissions.global.GlobalPermissionRepository
import com.polidea.shuttle.domain.user.permissions.project.ProjectPermissionRepository
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

// According to http://docs.spring.io/spring-boot/docs/1.4.0.RELEASE/reference/html/boot-features-testing.html#boot-features-testing-spring-boot-applications-excluding-config
//   `@TestConfiguration` class has are not picked up by scanning so we can explicitly decide when to use it.
// On the contrary `@Configuration` class will be picked up by scanning by default but it will be not in some cases:
//   eg. if it's subclass of Groovy `Specification` (which is annotated with `@RunWith`).
@TestConfiguration
class PermissionChecksConfigurationForTests {

    @Bean
    PermissionChecks permissionChecks(UserRepository userRepository,
                                      ProjectRepository projectRepository,
                                      AppRepository appRepository,
                                      GlobalPermissionRepository globalPermissionRepository,
                                      ProjectPermissionRepository projectPermissionRepository) {
        new PermissionChecks(
                userRepository,
                projectRepository,
                appRepository,
                globalPermissionRepository,
                projectPermissionRepository
        )
    }

}
