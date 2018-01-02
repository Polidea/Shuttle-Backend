package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.app.App
import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.user.permissions.PermissionType

import static com.polidea.shuttle.domain.app.Platform.determinePlatformFromText
import static org.hamcrest.Matchers.empty
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasSize
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ClientProjectsControllerSpec extends MockMvcIntegrationSpecification {

    def "should get projects"() {
        given:
        def projectAssigneeToken = createAndAuthenticateUser('assignee@project')
        def project = createProject('Mega Project', 'mega_project_icon')
        assignUserToProject('assignee@project', project)
        assignProjectPermission(project, 'assignee@project', PermissionType.PUBLISHER)
        createUser('member@project', 'Project Member', 'project_member_avatar')
        addMemberToProject(project, 'member@project')

        when:
        def result = get('/projects', projectAssigneeToken)

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.projects', hasSize(1)))
              .andExpect(jsonPath('$.projects[0].id', equalTo(project.id)))
              .andExpect(jsonPath('$.projects[0].name', equalTo('Mega Project')))
              .andExpect(jsonPath('$.projects[0].iconHref', equalTo('mega_project_icon')))
              .andExpect(jsonPath('$.projects[0].permissions.canArchive', equalTo(false)))
              .andExpect(jsonPath('$.projects[0].permissions.canMute', equalTo(true)))
              .andExpect(jsonPath('$.projects[0].latestIosBuilds', hasSize(0)))
              .andExpect(jsonPath('$.projects[0].latestAndroidBuilds', hasSize(0)))
              .andExpect(jsonPath('$.projects[0].teamMembers', hasSize(1)))
              .andExpect(jsonPath('$.projects[0].teamMembers[0].email', equalTo('member@project')))
              .andExpect(jsonPath('$.projects[0].teamMembers[0].name', equalTo('Project Member')))
              .andExpect(jsonPath('$.projects[0].teamMembers[0].avatarHref', equalTo('project_member_avatar')))
    }

    def "should get archived projects"() {
        given:
        def projectArchiverToken = createAndAuthenticateUser('archiver@project')
        def project = createProject('Mega Project', 'mega_project_icon')
        assignProjectPermission(project, 'archiver@project', PermissionType.ARCHIVER)
        assignUserToProject('archiver@project', project)
        createUser('member@project', 'Project Member', 'project_member_avatar')
        addMemberToProject(project, 'member@project')
        post("/projects/${project.id}/archive", projectArchiverToken)

        when:
        def result = get('/projects/archived', projectArchiverToken)

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.projects', hasSize(1)))
              .andExpect(jsonPath('$.projects[0].id', equalTo(project.id)))
              .andExpect(jsonPath('$.projects[0].name', equalTo('Mega Project')))
              .andExpect(jsonPath('$.projects[0].iconHref', equalTo('mega_project_icon')))
    }

    def "archived projects should not include project which is no longer assigned"() {
        given:
        def projectArchiverToken = createAndAuthenticateUser('archiver@project')
        def project = createProject('Mega Project', 'mega_project_icon')
        assignProjectPermission(project, 'archiver@project', PermissionType.ARCHIVER)
        assignUserToProject('archiver@project', project)
        createUser('member@project', 'Project Member', 'project_member_avatar')
        addMemberToProject(project, 'member@project')
        post("/projects/${project.id}/archive", projectArchiverToken)

        and:
        unassignUserFromProject('archiver@project', project)

        when:
        def result = get('/projects/archived', projectArchiverToken)

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.projects', empty()))
    }

    def "should archive project if user has Project Permission to archive"() {
        given:
        def projectArchiverToken = createAndAuthenticateUser('archiver@project')
        def project = createProject('Mega Project', 'mega_project_icon')
        assignProjectPermission(project, 'archiver@project', PermissionType.ARCHIVER)
        assignUserToProject('archiver@project', project)

        when:
        def result = post("/projects/${project.id}/archive", projectArchiverToken)

        then:
        result.andExpect(status().isNoContent())

        and:
        get('/projects', projectArchiverToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.projects', empty()))
        get('/projects/archived', projectArchiverToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.projects', hasSize(1)))
    }

    def "should archive project if user has Global Permission to archive"() {
        given:
        def globalArchiverToken = createAndAuthenticateUser('archiver@global')
        def project = createProject('Mega Project', 'mega_project_icon')
        assignGlobalPermissions('archiver@global', PermissionType.ARCHIVER)
        assignUserToProject('archiver@global', project)

        when:
        def result = post("/projects/${project.id}/archive", globalArchiverToken)

        then:
        result.andExpect(status().isNoContent())

        and:
        get('/projects', globalArchiverToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.projects', empty()))
        get('/projects/archived', globalArchiverToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.projects', hasSize(1)))
    }

    def "should not archive project which does not exist"() {
        given:
        def globalArchiverToken = createAndAuthenticateUser('archiver@global')
        assignGlobalPermissions('archiver@global', PermissionType.ARCHIVER)
        def idOfNonExistentProject = 123

        when:
        def result = post("/projects/${idOfNonExistentProject}/archive", globalArchiverToken)

        then:
        result.andExpect(status().isNotFound())
              .andExpect(jsonPath('$.code', equalTo(2003)))
              .andExpect(jsonPath('$.message', equalTo('Project not found')))
    }

    def "should not archive project if user has no permission to archive"() {
        given:
        def projectAssigneeToken = createAndAuthenticateUser('assignee@project')
        def project = createProject('Mega Project', 'mega_project_icon')
        assignUserToProject('assignee@project', project)

        when:
        def result = post("/projects/${project.id}/archive", projectAssigneeToken)

        then:
        result.andExpect(status().isForbidden())
    }

    def "should unarchive project (even if User has no permission to archive)"() {
        given:
        def userToken = createAndAuthenticateUser('user@project')
        def project = createProject('Mega Project', 'mega_project_icon')
        assignProjectPermission(project, 'user@project', PermissionType.ARCHIVER)
        assignUserToProject('user@project', project)

        and:
        archiveProject('user@project', project)
        unassignProjectPermission(project, 'user@project', PermissionType.ARCHIVER)


        when:
        def result = post("/projects/${project.id}/unarchive", userToken)

        then:
        result.andExpect(status().isNoContent())

        and:
        get('/projects', userToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.projects', hasSize(1)))
        get('/projects/archived', userToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.projects', empty()))
    }

    def "should not unarchive project if User is no longer assigned to it"() {
        given:
        def userToken = createAndAuthenticateUser('user@project')
        def project = createProject('Mega Project', 'mega_project_icon')
        assignProjectPermission(project, 'user@project', PermissionType.ARCHIVER)
        assignUserToProject('user@project', project)

        and:
        archiveProject('user@project', project)
        unassignUserFromProject('user@project', project)

        when:
        def result = post("/projects/${project.id}/unarchive", userToken)

        then:
        result.andExpect(status().isNotFound())
    }

    def "should not unarchive project which does not exist"() {
        given:
        def userToken = createAndAuthenticateUser('user@project')
        def project = createProject('Mega Project', 'mega_project_icon')
        assignProjectPermission(project, 'user@project', PermissionType.ARCHIVER)
        assignUserToProject('user@project', project)

        and:
        archiveProject('user@project', project)
        deleteProject(project)

        when:
        def result = post("/projects/${project.id}/unarchive", userToken)

        then:
        result.andExpect(status().isNotFound())
    }

    def "should not include deleted Members of Projects"() {
        given:
        def projectAssigneeToken = createAndAuthenticateUser('assignee@project')
        def project = createProject('Mega Project', 'mega_project_icon')
        assignUserToProject('assignee@project', project)
        assignProjectPermission(project, 'assignee@project', PermissionType.PUBLISHER)
        createUser('member@project', 'Project Member', 'project_member_avatar')
        addMemberToProject(project, 'member@project')

        and:
        deleteUser('member@project')

        when:
        def result = get('/projects', projectAssigneeToken)

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.projects', hasSize(1)))
              .andExpect(jsonPath('$.projects[0].id', equalTo(project.id)))
              .andExpect(jsonPath('$.projects[0].teamMembers', hasSize(0)))
    }

    private String createAndAuthenticateUser(String userEmail) {
        return setupHelper.createAndAuthenticateClientUser(userEmail, "Name of ${userEmail}", null, 'any-device-id')
    }

    private void createUser(String userEmail, String name, String avatarHref) {
        setupHelper.createUser(userEmail, name, avatarHref)
    }

    private void deleteUser(String userEmail) {
        setupHelper.deleteUser(userEmail)
    }

    private assignGlobalPermissions(String userEmail, PermissionType permission) {
        setupHelper.assignGlobalPermissions(userEmail, [permission])
    }

    private assignProjectPermission(Project project, String userEmail, PermissionType permission) {
        setupHelper.assignProjectPermission(project, userEmail, permission)
    }

    private unassignProjectPermission(Project project, String userEmail, PermissionType permission) {
        setupHelper.unassignProjectPermission(project, userEmail, permission)
    }

    private Project createProject(String name, String iconHref) {
        setupHelper.createProject(name, iconHref)
    }

    private Project deleteProject(Project project) {
        setupHelper.deleteProject(project.id)
    }

    private assignUserToProject(String userEmail, Project project) {
        setupHelper.assignUserToProject(userEmail, project.id)
    }

    private unassignUserFromProject(String userEmail, Project project) {
        setupHelper.unassignUserFromProject(userEmail, project.id)
    }

    private addMemberToProject(Project project, String userEmail) {
        setupHelper.addMemberToProject(project.id, userEmail)
    }

    private archiveProject(String userEmail, Project project) {
        setupHelper.archiveProject(userEmail, project.id)
    }

    private App createApp(Project project, String platform, String appId, String name) {
        setupHelper.createApp(project, determinePlatformFromText(platform), appId, name, null)
    }

    private createBuild(String appId, String platform, String buildIdentifier) {
        def releaserEmail = 'releaser@shuttle.com'
        createUser(releaserEmail, 'Build Releaser', null)
        setupHelper.createBuild(
                appId,
                determinePlatformFromText(platform),
                releaserEmail,
                buildIdentifier,
                '1.2.3',
                'Any Release Notes',
                'http://href.to.app',
                123456789
        )
    }

}
