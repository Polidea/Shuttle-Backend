package com.polidea.shuttle.domain.user.project

import com.polidea.shuttle.domain.project.ProjectNotFoundException
import com.polidea.shuttle.domain.project.ProjectService
import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.UserNotFoundException
import com.polidea.shuttle.domain.user.UserProjectService
import com.polidea.shuttle.domain.user.UserService
import com.polidea.shuttle.domain.user.permissions.project.ProjectPermissionService
import spock.lang.Specification

import static com.polidea.shuttle.TestConstants.TEST_EMAIL
import static com.polidea.shuttle.TestConstants.TEST_PROJECT_ID

class UserProjectServiceSpec extends Specification {

    UserService userServiceMock = Mock(UserService)
    ProjectService projectService = Mock(ProjectService)
    ProjectPermissionService projectPermissionService = Mock(ProjectPermissionService)

    UserProjectService userProjectService

    void setup() {
        this.userProjectService =
                new UserProjectService(userServiceMock,
                                       projectService,
                                       projectPermissionService)
    }

    def "should throw exception when trying to assign nonexistent user"() {
        given:

        when:
        userServiceMock.findUser(TEST_EMAIL) >> { throw new UserNotFoundException(TEST_EMAIL) }
        userProjectService.assignUserToProject(TEST_EMAIL, TEST_PROJECT_ID)

        then:
        thrown(UserNotFoundException)
    }

    def "should throw exception when one of the projects is not found while assigning"() {
        when:
        userServiceMock.findUser(TEST_EMAIL) >> new User(id: 1, email: TEST_EMAIL)
        projectService.findProject(_) >> { throw new ProjectNotFoundException() }
        userProjectService.assignUserToProject("", TEST_PROJECT_ID)

        then:
        thrown(ProjectNotFoundException)
    }

    def "should throw exception when trying to unassign nonexistent user"() {
        when:
        userServiceMock.findUser(TEST_EMAIL) >> { throw new UserNotFoundException(TEST_EMAIL) }
        userProjectService.unassignUserFromProject(TEST_EMAIL, TEST_PROJECT_ID)

        then:
        thrown(UserNotFoundException)
    }

    def "should throw exception when one of the apps is not found while unassigning"() {
        given:
        def nonexistentProjectId = 100

        when:
        projectService.findProject(nonexistentProjectId) >> { throw new ProjectNotFoundException() }
        userProjectService.unassignUserFromProject(TEST_EMAIL, nonexistentProjectId)

        then:
        thrown(ProjectNotFoundException)
    }

    def "should throw exception if a requested user is not found"() {
        when:
        userServiceMock.findUser(TEST_EMAIL) >> { throw new UserNotFoundException(TEST_EMAIL) }
        userServiceMock.findUser('email2@email.com') >> new User('email2@email.com', 'User 2')
        userProjectService.assignUserToProject(TEST_EMAIL, TEST_PROJECT_ID)
        userProjectService.assignUserToProject('email2@email.com', TEST_PROJECT_ID)

        then:
        thrown(UserNotFoundException)
    }

    def "should throw exception if app is not found"() {
        when:
        userServiceMock.findUser(TEST_EMAIL) >> new User(TEST_EMAIL, 'User')
        1 * projectService.findProject(TEST_PROJECT_ID) >> { throw new ProjectNotFoundException() }
        userProjectService.assignUserToProject(TEST_EMAIL, TEST_PROJECT_ID)
        userProjectService.assignUserToProject('email2@email.com', TEST_PROJECT_ID)

        then:
        thrown(ProjectNotFoundException)
    }

}
