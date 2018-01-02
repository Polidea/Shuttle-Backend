package com.polidea.shuttle.domain.app

import com.polidea.shuttle.domain.app.input.AppAdditionRequest
import com.polidea.shuttle.domain.app.input.AppEditionRequest
import com.polidea.shuttle.domain.build.BuildRepository
import com.polidea.shuttle.domain.project.Project
import com.polidea.shuttle.domain.project.ProjectRepository
import com.polidea.shuttle.domain.user.UserRepository
import com.polidea.shuttle.infrastructure.security.authorization.PermissionChecks
import spock.lang.Specification

import static Platform.ANDROID
import static com.polidea.shuttle.TestConstants.TEST_APP_ID
import static com.polidea.shuttle.TestConstants.TEST_APP_NAME
import static com.polidea.shuttle.TestConstants.TEST_PROJECT_ID

class AppServiceSpec extends Specification {

    static
    final AppAdditionRequest APPLICATION_REQUEST = new AppAdditionRequest(name: TEST_APP_NAME, iconHref: "http://some.com/icon.png")

    AppService appService

    AppRepository appRepository = Mock(AppRepository)
    ProjectRepository projectRepository = Mock(ProjectRepository)
    PermissionChecks permissionChecks = Mock(PermissionChecks)
    UserRepository userRepository = Mock(UserRepository)
    BuildRepository buildRepository = Mock(BuildRepository)

    void setup() {
        appService = new AppService(appRepository, projectRepository, permissionChecks, buildRepository, userRepository)
    }

    def "should throw DuplicateAppException when the same app on the same platform is added"() {
        given:
        projectRepository.findById(TEST_PROJECT_ID) >> Optional.of(new Project('Any Project'))

        when:
        appRepository.find(ANDROID, TEST_APP_ID) >> Optional.of(new App())
        appService.addApp(APPLICATION_REQUEST, TEST_PROJECT_ID, ANDROID, TEST_APP_ID)

        then:
        DuplicateAppException thrownException = thrown()
        thrownException.getMessage() == "App for platform '${ANDROID.toString()}' with appId '$TEST_APP_ID' already exists" as String
    }

    def "should throw exception when app is not found by id"() {
        when:
        appService.findApp(ANDROID, TEST_APP_ID)

        then:
        appRepository.find(ANDROID, TEST_APP_ID) >> Optional.empty()
        thrown(AppNotFoundException)
    }

    def "should delete app"() {
        given:
        def app = new App(id: 1, appId: 'any.app.id')
        appRepository.find(ANDROID, TEST_APP_ID) >> Optional.of(app)

        when:
        appService.delete(TEST_APP_ID, ANDROID)

        then:
        1 * appRepository.delete(app)
    }

    def "should throw exception when application to delete is not present"() {
        given:
        appRepository.find(ANDROID, TEST_APP_ID) >> Optional.empty()

        when:
        appService.delete(TEST_APP_ID, ANDROID)

        then:
        0 * appRepository.delete(_)
        thrown(AppNotFoundException)
    }

    def "should throw EntityNotFoundException when there is no app with such type and id while editing"() {
        when:
        appRepository.find(ANDROID, TEST_APP_ID) >> Optional.empty()
        appService.editApp(new AppEditionRequest(name: "newName"), ANDROID, TEST_APP_ID)

        then:
        thrown(AppNotFoundException)
    }

    def "should edit app name successfully"() {
        given:
        def savedApp = new App(id: 1, appId: TEST_APP_ID, name: TEST_APP_NAME)
        when:
        appRepository.find(ANDROID, TEST_APP_ID) >> Optional.of(savedApp)
        appService.editApp(new AppEditionRequest(name: "newName"), ANDROID, TEST_APP_ID)

        then:
        savedApp.appId == TEST_APP_ID
        savedApp.name == "newName"
    }

    def "should find desired app"() {
        given:
        App app = new App(id: 1, appId: TEST_APP_ID, platform: ANDROID)

        when:
        appService.findApp(ANDROID, TEST_APP_ID)

        then:
        1 * appRepository.find(ANDROID, TEST_APP_ID) >> Optional.of(app)
    }

    def "should throw EntityNotFoundException when there is no such app"() {
        when:
        appRepository.find(ANDROID, TEST_APP_ID) >> Optional.empty()
        appService.findApp(ANDROID, TEST_APP_ID)

        then:
        thrown(AppNotFoundException)
    }

    def "should return not null app response"() {
        given:
        App app = new App(id: 1, appId: TEST_APP_ID, platform: ANDROID)

        when:
        appRepository.find(ANDROID, TEST_APP_ID) >> Optional.of(app)
        def result = appService.findApp(ANDROID, TEST_APP_ID)

        then:
        result != null
    }
}
