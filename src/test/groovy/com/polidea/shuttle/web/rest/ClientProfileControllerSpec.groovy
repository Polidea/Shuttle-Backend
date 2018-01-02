package com.polidea.shuttle.web.rest

import com.polidea.shuttle.infrastructure.external_storage.ExternalStorage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import spock.lang.Unroll

import static com.polidea.shuttle.test_config.DefaultAvatarsConfigurationForTests.NEXT_RANDOM_DEFAULT_AVATAR_URL
import static com.polidea.shuttle.test_config.ExternalStorageConfigurationForTests.UPLOADED_FILE_URL_IN_TESTS
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE

class ClientProfileControllerSpec extends HttpIntegrationSpecification {

    private final static long MAX_ALLOWED_BYTES = 10 * 1024 * 1024

    @Autowired
    private ResourceLoader resourceLoader
    @Autowired
    private ExternalStorage externalStorage

    @Unroll
    def "should get Profile (avatarHref: #avatarHref)"(String avatarHref,
                                                       String assignedAvatarHref) {
        given:
        def userEmail = 'any.user@shuttle.com'
        createUser(userEmail, 'Any User', avatarHref)
        def userToken = createClientAccessTokenFor(userEmail)

        when:
        def profileResponse = get('/profile', userToken)

        then:
        profileResponse.code() == 200
        profileResponse.body().email == userEmail
        profileResponse.body().name == 'Any User'
        profileResponse.body().avatarHref == assignedAvatarHref

        where:
        avatarHref        | assignedAvatarHref
        'any.avatar.href' | 'any.avatar.href'
        null              | NEXT_RANDOM_DEFAULT_AVATAR_URL
    }

    def "should fail to get Profile if is not logged in"() {
        when:
        def profileResponse = getWithoutAccessToken('/profile')

        then:
        profileResponse.code() == 403
    }

    @Unroll
    def "should update Profile Name (name: #updatedName)"(String updatedName) {
        given:
        def userEmail = 'any.user@shuttle.com'
        createUser(userEmail, 'User BEFORE', 'avatar.href.BEFORE')
        def userToken = createClientAccessTokenFor(userEmail)

        when:
        def updateProfileResponse = patch('/profile', [
                name: updatedName
        ], userToken)

        then:
        updateProfileResponse.code() == 204

        and:
        def getProfileResponse = get('/profile', userToken)
        getProfileResponse.code() == 200
        getProfileResponse.body().name == updatedName
        getProfileResponse.body().avatarHref == 'avatar.href.BEFORE'

        where:
        updatedName << ['User AFTER']
    }

    @Unroll
    def "should fail to update Profile Name with invalid value (invalid name: #invalidName)"(String invalidName) {
        given:
        def userEmail = 'any.user@shuttle.com'
        createUser(userEmail, 'User BEFORE', 'avatar.href.BEFORE')
        def userToken = createClientAccessTokenFor(userEmail)

        when:
        def updateProfileResponse = patch('/profile', [
                name: invalidName
        ], userToken)

        then:
        updateProfileResponse.code() == 400

        and:
        def getProfileResponse = get('/profile', userToken)
        getProfileResponse.code() == 200
        getProfileResponse.body().name == 'User BEFORE'

        where:
        invalidName << ['', '    ', null]
    }

    @Unroll
    def "should update Profile Avatar (avatarHref: #updatedAvatarHref)"(String updatedAvatarHref) {
        given:
        def userEmail = 'any.user@shuttle.com'
        createUser(userEmail, 'User BEFORE', 'avatar.href.BEFORE')
        def userToken = createClientAccessTokenFor(userEmail)

        when:
        def updateProfileResponse = patch('/profile', [
                avatarHref: updatedAvatarHref
        ], userToken)

        then:
        updateProfileResponse.code() == 204

        and:
        def getProfileResponse = get('/profile', userToken)
        getProfileResponse.code() == 200
        getProfileResponse.body().name == 'User BEFORE'
        getProfileResponse.body().avatarHref == updatedAvatarHref

        where:
        updatedAvatarHref << ['avatar.href.AFTER', '', null]
    }

    def "should fail to update Profile if is not logged in"() {
        when:
        def updateProfileResponse = patchWithoutAccessToken('/profile', [
                name      : 'Updated Name',
                avatarHref: 'updated.avatar.href'
        ])

        then:
        updateProfileResponse.code() == 403
    }

    @Unroll
    def "should upload Avatar (mimeType: #avatarMimeType, avatar: #avatarResourcePath)"(String avatarMimeType,
                                                                                        String avatarResourcePath) {
        given:
        def userToken = createAndAuthenticateUser()

        when:
        def uploadAvatarResponse = postFileAsMultipartForm(
                '/profile/avatar',
                avatarMimeType,
                "avatarImage",
                "any_file_name",
                loadFileFromClasspath(avatarResourcePath),
                userToken
        )

        then:
        uploadAvatarResponse.code() == 200
        uploadAvatarResponse.body().avatarHref == UPLOADED_FILE_URL_IN_TESTS

        and:
        def getProfileResponse = get('/profile', userToken)
        getProfileResponse.code() == 200
        getProfileResponse.body().avatarHref == UPLOADED_FILE_URL_IN_TESTS

        where:
        avatarMimeType                          | avatarResourcePath
        "image/png"                             | "test_assets/PNG_image_for_upload_testing.png"
        "image/jpeg"                            | "test_assets/JPEG_image_for_upload_testing.jpg"
    }

    def "should fail to upload Avatar if is not logged in"() {
        when:
        def uploadAvatarResponse = postFileAsMultipartFormWithoutAccessToken(
                '/profile/avatar',
                IMAGE_PNG_VALUE,
                "avatarImage",
                "any_file_name.png",
                loadFileFromClasspath("test_assets/PNG_image_for_upload_testing.png")
        )

        then:
        uploadAvatarResponse.code() == 403
    }

    def "should fail to upload Avatar if is too big"() {
        given:
        def userToken = createAndAuthenticateUser()

        and:
        def tooBigFile = fileMockWithSizeInBytesOf(MAX_ALLOWED_BYTES + 1)

        when:
        def uploadAvatarResponse = postFileAsMultipartForm(
                '/profile/avatar',
                IMAGE_PNG_VALUE,
                "avatarImage",
                "any_file_name.png",
                tooBigFile,
                userToken
        )

        then:
        uploadAvatarResponse.code() == 400
        uploadAvatarResponse.body().code == 2018
        uploadAvatarResponse.body().message ==
                "Avatar size is too big, because it has ${tooBigFile.size()} bytes and it's more than allowed ${MAX_ALLOWED_BYTES} bytes"
    }

    def "should succeed to upload Avatar if is not too big"() {
        given:
        def userToken = createAndAuthenticateUser()

        and:
        def notTooBigFile = fileMockWithSizeInBytesOf(MAX_ALLOWED_BYTES)

        when:
        def uploadAvatarResponse = postFileAsMultipartForm(
                '/profile/avatar',
                IMAGE_PNG_VALUE,
                "avatarImage",
                "any_file_name.png",
                notTooBigFile,
                userToken
        )

        then:
        uploadAvatarResponse.code() == 200
    }

    def "should fail to upload Avatar if is empty"() {
        given:
        def userToken = createAndAuthenticateUser()

        and:
        def emptyFile = fileMockWithSizeInBytesOf(0)

        when:
        def uploadAvatarResponse = postFileAsMultipartForm(
                '/profile/avatar',
                IMAGE_PNG_VALUE,
                "avatarImage",
                "any_file_name.png",
                emptyFile,
                userToken
        )

        then:
        uploadAvatarResponse.code() == 400
        uploadAvatarResponse.body().code == 2019
        uploadAvatarResponse.body().message == "Avatar is empty (has size of 0 bytes)"
    }

    private String createClientAccessTokenFor(String userEmail) {
        setupHelper.createClientAccessToken(userEmail, 'any-device-id')
    }

    private void createUser(String userEmail, String name, String avatarHref) {
        setupHelper.createUser(userEmail, name, avatarHref)
    }

    private File loadFileFromClasspath(String pathToFile) {
        return resourceLoader.getResource("classpath:" + pathToFile).getFile()
    }

    private String createAndAuthenticateUser() {
        def userEmail = 'any.user@shuttle.com'
        createUser(userEmail, 'Any User', 'http://old.avatar/href')
        return createClientAccessTokenFor(userEmail)
    }

    private File fileMockWithSizeInBytesOf(long bytesSize) {
        File file = GroovyMock(File)
        file.getBytes() >> new byte[bytesSize]
        file.size() >> bytesSize
        return file
    }

}
