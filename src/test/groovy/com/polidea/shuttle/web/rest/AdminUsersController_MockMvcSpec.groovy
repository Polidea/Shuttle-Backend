package com.polidea.shuttle.web.rest

import com.polidea.shuttle.domain.user.User
import com.polidea.shuttle.domain.user.UserJpaRepository
import com.polidea.shuttle.domain.user.UserRepository
import com.polidea.shuttle.domain.user.permissions.PermissionType
import com.polidea.shuttle.infrastructure.mail.MailAuthService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

import static com.polidea.shuttle.test_config.DefaultAvatarsConfigurationForTests.NEXT_RANDOM_DEFAULT_AVATAR_URL
import static groovy.json.JsonOutput.toJson
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasSize
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AdminUsersController_MockMvcSpec extends MockMvcIntegrationSpecification {

    @Autowired
    UserRepository userRepository
    @Autowired
    UserJpaRepository userJpaRepository
    @Autowired
    MailAuthService mailAuthService

    String adminToken

    @Override
    void setup() {
        createUser('admin@user.com')
        assignGlobalPermissions('admin@user.com', [PermissionType.ADMIN])
        adminToken = createAccessTokenFor('admin@user.com')
    }

    def "should add a new User with obligatory properties only"() {
        when:
        def userEmail = 'new@user.com'
        def result = post('/admin/users', toJson([
                email: userEmail,
                name : 'New User'
        ]), adminToken)

        then:
        result.andExpect(status().isNoContent())

        and:
        get("/admin/users/${userEmail}", adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.email').value(userEmail))
                .andExpect(jsonPath('$.name').value('New User'))
                .andExpect(jsonPath('$.isVisibleForModerator').value(false))
                .andExpect(jsonPath('$.avatarHref').value(NEXT_RANDOM_DEFAULT_AVATAR_URL))
                .andExpect(jsonPath('$.globalPermissions').value('can_mute'))
                .andExpect(jsonPath('$.projects').isEmpty())
    }

    @Unroll
    def "should add new User with isVisibleForModerator set to #isVisibleForModerator"(boolean isVisibleForModerator) {
        when:
        def userEmail = 'new@user.com'
        def result = post('/admin/users', toJson([
                email                : userEmail,
                name                 : 'New User',
                isVisibleForModerator: isVisibleForModerator
        ]), adminToken)

        then:
        result.andExpect(status().isNoContent())

        and:
        get("/admin/users/${userEmail}", adminToken)
                .andExpect(jsonPath('$.isVisibleForModerator').value(isVisibleForModerator))

        where:
        isVisibleForModerator << [true, false]
    }

    def "should add a new User with all properties set"() {
        when:
        def userEmail = 'new@user.com'
        def result = post('/admin/users', toJson([
                email     : userEmail,
                name      : 'New User',
                avatarHref: 'avatar_of_the_new_user'
        ]), adminToken)

        then:
        result.andExpect(status().isNoContent())

        and:
        get("/admin/users/${userEmail}", adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.email').value(userEmail))
                .andExpect(jsonPath('$.name').value('New User'))
                .andExpect(jsonPath('$.isVisibleForModerator').value(false))
                .andExpect(jsonPath('$.avatarHref').value('avatar_of_the_new_user'))
                .andExpect(jsonPath('$.globalPermissions').value('can_mute'))
                .andExpect(jsonPath('$.projects').isEmpty())
    }

    def "should set isVisibleForModerator properly"() {
        when:
        def userEmail = 'new@user.com'
        def result = post('/admin/users', toJson([
                email     : userEmail,
                name      : 'New User',
                avatarHref: 'avatar_of_the_new_user'
        ]), adminToken)
        patch("/admin/users/${userEmail}", toJson([
                isVisibleForModerator: true
        ]), adminToken)

        then:
        result.andExpect(status().isNoContent())

        and:
        get("/admin/users/${userEmail}", adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.email').value(userEmail))
                .andExpect(jsonPath('$.isVisibleForModerator').value(true))
    }

    def "should send an invitation e-mail when new User is created"() {
        when:
        def result = post('/admin/users', toJson([
                email: 'new@user.com',
                name : 'New User'
        ]), adminToken)

        then:
        result.andExpect(status().isNoContent())

        and:
        1 * mailAuthService.sendInvitationEmail('new@user.com')
    }

    @Unroll
    def "should fail to add an User with invalid e-mail '#invalidEmail'"(String invalidEmail) {
        when:
        def result = post('/admin/users', toJson([
                email: invalidEmail,
                name : 'Any User'
        ]), adminToken)

        then:
        result.andExpect(status().isBadRequest())

        where:
        invalidEmail << ['totally-not-a-valid-email', '', null]
    }

    def "should fail to add an User twice"() {
        given:
        createUser('john@user.com')

        when:
        def result = post('/admin/users', toJson([
                email: 'john@user.com',
                name : 'John'
        ]), adminToken)

        then:
        result.andExpect(status().isConflict())
              .andExpect(jsonPath('$.code', equalTo(2009)))
              .andExpect(jsonPath('$.message', equalTo("User with e-mail 'john@user.com' already exists" as String)))
    }

    @Unroll
    def "should not add a new User if has no privilege to do so (permission: #nonAdminPermission)"(PermissionType nonAdminPermission) {
        given:
        createUser('non-admin@user.com')
        assignGlobalPermissions('non-admin@user.com', [nonAdminPermission])
        def nonAdminToken = createAccessTokenFor('non-admin@user.com')

        when:
        def result = post('/admin/users', toJson([
                email: 'new@user.com',
                name : 'New User'
        ]), nonAdminToken)

        then:
        result.andExpect(status().isForbidden())

        where:
        nonAdminPermission << PermissionType.values() - PermissionType.ADMIN
    }

    def "should delete an existing User"() {
        given:
        createUser('john@user.com')

        when:
        def result = delete('/admin/users/john@user.com', adminToken)

        then:
        result.andExpect(status().isNoContent())

        and:
        get('/admin/users', adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.users', hasSize(1)))
                .andExpect(jsonPath('$.users[0].email').value('admin@user.com'))
    }

    def "when deleting an User we should mark him it as deleted but keep instance in a database"() {
        given:
        def userEmail = 'john@user.com'
        createUser(userEmail)

        when:
        def result = delete("/admin/users/${userEmail}", adminToken)

        then:
        result.andExpect(status().isNoContent())

        and:
        userRepository.findUser(userEmail) == Optional.empty()

        and:
        deletedUsers().size() == 1
        deletedUsers().get(0).email() == userEmail
        deletedUsers().get(0).isDeleted() == true
    }

    def "should fail to delete an User which does not exist"() {
        when:
        def result = delete('/admin/users/john@user.com', adminToken)

        then:
        result.andExpect(status().isNotFound())
              .andExpect(jsonPath('$.code', equalTo(2001)))
              .andExpect(jsonPath('$.message', equalTo("User 'john@user.com' does not exist" as String)))

        and:
        deletedUsers().size() == 0
    }

    @Unroll
    def "should not delete an User if has no privilege to do so (permission: #nonAdminPermission)"(PermissionType nonAdminPermission) {
        given:
        createUser('non-admin@user.com')
        assignGlobalPermissions('non-admin@user.com', [nonAdminPermission])
        def nonAdminToken = createAccessTokenFor('non-admin@user.com')

        and:
        def userEmail = 'john@user.com'
        createUser(userEmail)

        when:
        def result = delete("/admin/users/${userEmail}", nonAdminToken)

        then:
        result.andExpect(status().isForbidden())

        and:
        deletedUsers().size() == 0

        where:
        nonAdminPermission << PermissionType.values() - PermissionType.ADMIN
    }

    def "should create a fresh new User of same e-mail as already deleted User"() {
        given:
        def userEmail = 'emotionally.unstable@user.com'
        createUser(userEmail, 'Happy User', null)

        and:
        delete("/admin/users/${userEmail}", adminToken)
                .andExpect(status().isNoContent())

        when:
        def result = post('/admin/users', toJson([
                email: userEmail,
                name : 'Sad User'
        ]), adminToken)

        then:
        result.andExpect(status().isNoContent())

        and:
        get('/admin/users', adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.users', hasSize(2)))
                .andExpect(jsonPath('$.users[?(@.email=="admin@user.com")].email').value('admin@user.com'))
                .andExpect(jsonPath('$.users[?(@.email=="emotionally.unstable@user.com")].email').value(userEmail))
                .andExpect(jsonPath('$.users[?(@.email=="emotionally.unstable@user.com")].name').value('Sad User'))

        and:
        deletedUsers().size() == 1
        deletedUsers().get(0).email == userEmail
        deletedUsers().get(0).name == 'Happy User'
    }

    def "should be able to delete many Users with same e-mail"() {
        given:
        def userEmail = 'forever.alive@user.com'
        post('/admin/users', toJson([
                email: userEmail,
                name : 'Any User'
        ]), adminToken).andExpect(status().isNoContent())

        and:
        delete("/admin/users/${userEmail}", adminToken)
                .andExpect(status().isNoContent())

        and:
        post('/admin/users', toJson([
                email: userEmail,
                name : 'Any User'
        ]), adminToken).andExpect(status().isNoContent())

        when:
        def result = delete("/admin/users/${userEmail}", adminToken)

        then:
        result.andExpect(status().isNoContent())

        and:
        deletedUsers().size() == 2
        deletedUsers()*.email == [userEmail, userEmail]
    }

    private void createUser(String userEmail) {
        setupHelper.createUser(userEmail, "Name of ${userEmail}", null)
    }

    private void createUser(String userEmail, String name, String avatarHref) {
        setupHelper.createUser(userEmail, name, avatarHref)
    }

    private String createAccessTokenFor(String userEmail) {
        return setupHelper.createClientAccessToken(userEmail, 'any-device-id')
    }

    private void assignGlobalPermissions(String userEmail, List<PermissionType> permissions) {
        setupHelper.assignGlobalPermissions(userEmail, permissions)
    }

    private List<User> deletedUsers() {
        return userJpaRepository.findAll().findAll({ it.isDeleted() })
    }

}
