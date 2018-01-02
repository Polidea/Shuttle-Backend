package com.polidea.shuttle.domain.user.output.factories

import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.output.AdminProjectAssigneeListResponse
import com.polidea.shuttle.domain.user.output.AdminProjectAssigneeResponse
import com.polidea.shuttle.domain.user.output.AdminUserListResponse
import com.polidea.shuttle.domain.user.output.AdminUserResponse
import com.polidea.shuttle.domain.user.permissions.global.GlobalPermissionsService
import com.polidea.shuttle.domain.user.permissions.project.ProjectPermissionService
import spock.lang.Specification

import static com.polidea.shuttle.TestConstants.TEST_PROJECT_ID
import static com.polidea.shuttle.TestConstants.TEST_PROJECT_NAME

class UserListResponseFactorySpec extends Specification {

    UserListResponseFactory userListResponseFactory

    void setup() {
        def globalPermissionsService = Mock(GlobalPermissionsService) { findFor(_) >> [] }
        def projectPermissionService = Mock(ProjectPermissionService) {
            findFor(_) >> []
            findFor(_, _) >> []
        }
        userListResponseFactory = new UserListResponseFactory(
                globalPermissionsService,
                projectPermissionService
        )
    }

    def "should create response for all users projects"() {
        given:
        def user = new User(id: 1, email: 'email1')
        def otherUser = new User(id: 2, email: 'email2')
        def project = new Project('Mega Project')

        when:
        AdminUserListResponse response = userListResponseFactory.createAdminUserListResponse(
                (user): [project] as Set,
                (otherUser): [] as Set
        )

        then:
        response.users.size() == 2
        response.users.get(0) instanceof AdminUserResponse
    }

    def "should create response for concrete project"() {
        given:
        User user = new User(id: 1, email: 'email1')
        User otherUser = new User(id: 2, email: 'email2')
        Project project = new Project(id: TEST_PROJECT_ID, name: TEST_PROJECT_NAME)
        project.rawAssignees = [user, otherUser]

        when:
        AdminProjectAssigneeListResponse response = userListResponseFactory.createAdminProjectAssigneeListResponse(project)

        then:
        response.users.size() == 2
        response.users.get(0) instanceof AdminProjectAssigneeResponse
    }

    def "should return empty list when there are no users"() {
        when:
        AdminUserListResponse response = userListResponseFactory.createAdminUserListResponse([:])

        then:
        response.users.size() == 0

    }
}
