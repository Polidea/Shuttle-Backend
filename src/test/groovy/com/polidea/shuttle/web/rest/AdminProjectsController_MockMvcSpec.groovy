package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.app.AppJpaRepository
import com.polidea.shuttle.domain.app.AppRepository
import com.polidea.shuttle.domain.build.BuildJpaRepository
import com.polidea.shuttle.domain.build.BuildRepository
import com.polidea.shuttle.domain.project.ProjectRepository
import com.polidea.shuttle.domain.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql

import static com.polidea.shuttle.TestConstants.PROJECTS_CONTROLLER_DATA_PATH
import static com.polidea.shuttle.TestConstants.TEST_EMAIL
import static com.polidea.shuttle.TestConstants.TEST_EMAIL_OTHER
import static com.polidea.shuttle.TestConstants.TEST_PROJECT_ID
import static com.polidea.shuttle.TestConstants.TEST_PROJECT_NAME_OTHER
import static com.polidea.shuttle.TestConstants.TEST_TOKEN
import static com.polidea.shuttle.TestConstants.TEST_TOKEN_USER
import static groovy.json.JsonOutput.toJson
import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.isEmptyString
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.notNullValue
import static org.hamcrest.Matchers.nullValue
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Sql(PROJECTS_CONTROLLER_DATA_PATH)
class AdminProjectsController_MockMvcSpec extends MockMvcIntegrationSpecification {

    static final String ENDPOINT = "/admin/projects"

    static
    final VALID_REQUEST = toJson([name: TEST_PROJECT_NAME_OTHER, iconHref: "http://a.com/a.png"])

    @Autowired
    ProjectRepository projectRepository

    @Autowired
    AppRepository appRepository

    @Autowired
    AppJpaRepository appJpaRepository

    @Autowired
    BuildRepository buildRepository

    @Autowired
    BuildJpaRepository buildJpaRepository

    @Autowired
    UserRepository userRepository

    def "should successfully add project"() {
        when:
        def result = post(ENDPOINT, VALID_REQUEST, TEST_TOKEN)

        then:
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.id', notNullValue()))

        and:
        // there were 2 projects, and was added 1
        projectRepository.findAll().size() == 3
    }

    def "should successfully remove project"() {
        when:
        def result = delete("$ENDPOINT/$TEST_PROJECT_ID", TEST_TOKEN)

        then:
        result.andExpect(status().isNoContent())

        and:
        // there were 2 projects, and was removed 1
        projectRepository.findAll().size() == 1
        appJpaRepository.findAll().count { it.isDeleted() == false } == 0
        buildJpaRepository.findAll().count { it.isDeleted() == false } == 0
    }

    def "should edit project name"() {
        given:
        String newProjectName = "newProjectName"
        String newIconHref = "http://img.com/new.png"
        def request = toJson([name: newProjectName, iconHref: newIconHref])

        when:
        def result = patch("$ENDPOINT/$TEST_PROJECT_ID", request, TEST_TOKEN)

        then:
        result.andExpect(status().isNoContent())
        projectRepository.findById(TEST_PROJECT_ID).get().name() == newProjectName
        projectRepository.findById(TEST_PROJECT_ID).get().iconHref() == newIconHref
    }

    def "should list all existing projects for admin"() {
        when:
        def result = get(ENDPOINT, TEST_TOKEN)

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.projects', hasSize(2)))
              .andExpect(jsonPath('$.projects[0].id', not(isEmptyString())))
              .andExpect(jsonPath('$.projects[0].name', not(isEmptyString())))
              .andExpect(jsonPath('$.projects[0].iconHref', not(isEmptyString())))
              .andExpect(jsonPath('$.projects[0].lastReleaseDate', notNullValue()))
              .andExpect(jsonPath('$.projects[0].teamMembers', hasSize(1)))
              .andExpect(jsonPath('$.projects[0].teamMembers[0].email', not(isEmptyString())))
              .andExpect(jsonPath('$.projects[0].teamMembers[0].name', not(isEmptyString())))
              .andExpect(jsonPath('$.projects[0].teamMembers[0].avatarHref', not(isEmptyString())))
    }

    def "should list existing projects assigned to user"() {
        when:
        def result = get(ENDPOINT, TEST_TOKEN_USER)

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.projects', hasSize(1)))
              .andExpect(jsonPath('$.projects[0].id', not(isEmptyString())))
              .andExpect(jsonPath('$.projects[0].name', not(isEmptyString())))
              .andExpect(jsonPath('$.projects[0].iconHref', not(isEmptyString())))
              .andExpect(jsonPath('$.projects[0].lastReleaseDate', nullValue()))
              .andExpect(jsonPath('$.projects[0].teamMembers', hasSize(1)))
              .andExpect(jsonPath('$.projects[0].teamMembers[0].email', not(isEmptyString())))
              .andExpect(jsonPath('$.projects[0].teamMembers[0].name', not(isEmptyString())))
              .andExpect(jsonPath('$.projects[0].teamMembers[0].avatarHref', not(isEmptyString())))
    }

    def "should add team member to project"() {
        when:
        def result = post("$ENDPOINT/$TEST_PROJECT_ID/members/$TEST_EMAIL_OTHER/", TEST_TOKEN)

        then:
        result.andExpect(status().isOk())
        // initially user was not member of project
        def members = projectRepository.findById(TEST_PROJECT_ID).get().members()
        members.find { it -> it.email() == TEST_EMAIL_OTHER } != null
    }

    def "should remove team member from project"() {
        when:
        def result = delete("$ENDPOINT/$TEST_PROJECT_ID/members/$TEST_EMAIL/", TEST_TOKEN)

        then:
        result.andExpect(status().isOk())
        // initially user was member of 1 project
        def members = projectRepository.findById(TEST_PROJECT_ID).get().members()
        members.find { it -> it.email() == TEST_EMAIL_OTHER } == null
    }

}
