package com.polidea.shuttle.web.rest

import com.polidea.shuttle.ShuttleApplication
import com.polidea.shuttle.infrastructure.SetupHelperForIntegrationTest
import com.polidea.shuttle.infrastructure.http.JsonHttpClientForTests
import com.polidea.shuttle.test_config.AwsS3ConfigurationForTests
import com.polidea.shuttle.test_config.DefaultAvatarsConfigurationForTests
import com.polidea.shuttle.test_config.ExternalStorageConfigurationForTests
import com.polidea.shuttle.test_config.MailConfigurationForTests
import com.polidea.shuttle.test_config.NotificationsConfigurationForTests
import com.polidea.shuttle.test_config.PermissionChecksConfigurationForTests
import com.polidea.shuttle.test_config.RandomTokenGeneratorConfigurationForTests
import com.polidea.shuttle.test_config.RandomVerificationCodesConfigurationForTests
import com.polidea.shuttle.test_config.SecurityConfigurationForTests
import com.polidea.shuttle.test_config.TimeServiceConfigurationForTests
import groovy.json.JsonOutput
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql
import spock.lang.Specification

import static com.polidea.shuttle.TestConstants.TRUNCATE_TABLES_SCRIPT_PATH
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@ActiveProfiles(['integrationTests'])
@SpringBootTest(classes = ShuttleApplication, webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = [
        SetupHelperForIntegrationTest,
        MailConfigurationForTests,
        PermissionChecksConfigurationForTests,
        TimeServiceConfigurationForTests,
        SecurityConfigurationForTests,
        ExternalStorageConfigurationForTests,
        AwsS3ConfigurationForTests,
        RandomVerificationCodesConfigurationForTests,
        RandomTokenGeneratorConfigurationForTests,
        DefaultAvatarsConfigurationForTests,
        NotificationsConfigurationForTests,
        JsonHttpClientForTests
])
@Sql(TRUNCATE_TABLES_SCRIPT_PATH)
abstract class HttpIntegrationSpecification extends Specification {

    @LocalServerPort
    int serverPort

    @Autowired
    JsonHttpClientForTests jsonHttpClient

    @Autowired
    protected SetupHelperForIntegrationTest setupHelper

    protected JsonHttpClientForTests.JsonResponse getWithoutAccessToken(String path) {
        jsonHttpClient.performGetRequest(
                serverUrlWith(path),
                headersWithoutAccessToken()
        )
    }

    protected JsonHttpClientForTests.JsonResponse get(String path, String accessToken) {
        jsonHttpClient.performGetRequest(
                serverUrlWith(path),
                headersWith(accessToken)
        )
    }

    protected JsonHttpClientForTests.JsonResponse post(String path, String accessToken) {
        jsonHttpClient.performPostRequest(
                serverUrlWith(path),
                headersWith(accessToken),
                emptyBody()
        )
    }

    protected JsonHttpClientForTests.JsonResponse postWithoutAccessToken(String path, Map requestBodyJson) {
        jsonHttpClient.performPostRequest(
                serverUrlWith(path),
                headersWithoutAccessToken(),
                JsonOutput.toJson(requestBodyJson)
        )
    }

    protected JsonHttpClientForTests.JsonResponse post(String path, Map requestBodyJson, String accessToken) {
        jsonHttpClient.performPostRequest(
                serverUrlWith(path),
                headersWith(accessToken),
                JsonOutput.toJson(requestBodyJson)
        )
    }

    protected JsonHttpClientForTests.JsonResponse postFileAsMultipartFormWithoutAccessToken(String path,
                                                                                            String mimeType,
                                                                                            String formFieldName,
                                                                                            String fileName,
                                                                                            File file) {
        jsonHttpClient.performPostRequest(
                serverUrlWith(path),
                headersWithoutAccessToken(),
                new JsonHttpClientForTests.FileAsMultipartForm(
                        mimeType,
                        formFieldName,
                        fileName,
                        file
                )
        )
    }

    protected JsonHttpClientForTests.JsonResponse postFileAsMultipartForm(String path,
                                                                          String mimeType,
                                                                          String formFieldName,
                                                                          String fileName,
                                                                          File file,
                                                                          String accessToken) {
        jsonHttpClient.performPostRequest(
                serverUrlWith(path),
                headersWith(accessToken),
                new JsonHttpClientForTests.FileAsMultipartForm(
                        mimeType,
                        formFieldName,
                        fileName,
                        file
                )
        )
    }

    protected JsonHttpClientForTests.JsonResponse patchWithoutAccessToken(String path, Map requestBodyJson) {
        jsonHttpClient.performPatchRequest(
                serverUrlWith(path),
                headersWithoutAccessToken(),
                JsonOutput.toJson(requestBodyJson)
        )
    }

    protected JsonHttpClientForTests.JsonResponse patch(String path, Map requestBodyJson, String accessToken) {
        jsonHttpClient.performPatchRequest(
                serverUrlWith(path),
                headersWith(accessToken),
                JsonOutput.toJson(requestBodyJson)
        )
    }

    protected JsonHttpClientForTests.JsonResponse deleteWithoutAccessToken(String path) {
        jsonHttpClient.performDeleteRequest(
                serverUrlWith(path),
                headersWithoutAccessToken()
        )
    }

    protected JsonHttpClientForTests.JsonResponse delete(String path, String accessToken) {
        jsonHttpClient.performDeleteRequest(
                serverUrlWith(path),
                headersWith(accessToken)
        )
    }

    private Map<String, String> headersWithoutAccessToken() {
        return [:]
    }

    private Map<String, String> headersWith(String accessToken) {
        return ['Access-Token': accessToken]
    }

    private String serverUrlWith(String path) {
        "http://localhost:${serverPort}${path}"
    }

    private String emptyBody() {
        return ''
    }
}
