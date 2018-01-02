package com.polidea.shuttle.domain.user.permissions.project

import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.project.ProjectNotFoundException
import com.polidea.shuttle.domain.project.ProjectRepository
import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.UserNotFoundException
import com.polidea.shuttle.domain.user.UserRepository
import spock.lang.Specification

import static com.polidea.shuttle.TestConstants.TEST_PROJECT_ID
import static com.polidea.shuttle.domain.user.permissions.PermissionType.PUBLISHER

class ProjectPermissionsServiceSpec extends Specification {

    private static final EMAIL = 'email@email.com'

    ProjectPermissionRepository projectPermissionRepository = Mock(ProjectPermissionRepository)
    UserRepository userRepository = Mock(UserRepository)
    ProjectRepository projectRepository = Mock(ProjectRepository)

    ProjectPermissionService projectPermissionsService

    void setup() {
        projectPermissionsService = new ProjectPermissionService(
                userRepository,
                projectRepository,
                projectPermissionRepository
        )
    }

    def "should throw exception when user is not found while assigning"() {
        given:
        userRepository.findUser(EMAIL) >> Optional.empty()

        when:
        projectPermissionsService.assignProjectPermission(PUBLISHER, EMAIL, TEST_PROJECT_ID)

        then:
        thrown(UserNotFoundException)
    }

    def "should throw exception when application is not found while assigning"() {
        given:
        userRepository.findUser(EMAIL) >> Optional.of(new User(EMAIL, 'User'))
        projectRepository.findById(TEST_PROJECT_ID) >> Optional.empty()

        when:
        projectPermissionsService.assignProjectPermission(PUBLISHER, EMAIL, TEST_PROJECT_ID)

        then:
        thrown(ProjectNotFoundException)
    }

    def "should save permissions when assigning goes well"() {
        given:
        userRepository.findUser(EMAIL) >> Optional.of(new User(EMAIL, 'User'))
        projectRepository.findById(TEST_PROJECT_ID) >> Optional.of(new Project())
        projectPermissionRepository.findByUserAndProjectAndPermission(_, _, _) >> Optional.empty()

        when:
        projectPermissionsService.assignProjectPermission(PUBLISHER, EMAIL, TEST_PROJECT_ID)

        then:
        1 * projectPermissionRepository.createProjectPermission(_, _, _)
    }

}
