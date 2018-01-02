package com.polidea.shuttle.web.rest

import com.polidea.shuttle.ShuttleApplication
import com.polidea.shuttle.infrastructure.SetupHelperForIntegrationTest
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.lang.Specification

import javax.transaction.Transactional

import static com.polidea.shuttle.TestConstants.TRUNCATE_TABLES_SCRIPT_PATH
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity

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
        NotificationsConfigurationForTests,
        DefaultAvatarsConfigurationForTests
])
@Transactional
@Sql(TRUNCATE_TABLES_SCRIPT_PATH)
abstract class MockMvcIntegrationSpecification extends Specification {

    private static final String ACCESS_TOKEN_HEADER = 'Access-Token'

    @Autowired
    private WebApplicationContext applicationContext

    @Autowired
    protected SetupHelperForIntegrationTest setupHelper

    protected MockMvc mockMvc

    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                                 .apply(springSecurity())
                                 .build()
    }

    protected ResultActions post(String endpoint, String token) {
        mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                                              .headers(headersWithToken(token)))
    }

    protected ResultActions post(String endpoint, String requestBody, String token) {
        mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                                              .headers(headersWithToken(token))
                                              .content(requestBody)
                                              .contentType(APPLICATION_JSON)
        )
    }

    protected ResultActions delete(String path, String token) {
        mockMvc.perform(MockMvcRequestBuilders.delete(path)
                                              .headers(headersWithToken(token))
                                              .contentType(APPLICATION_JSON))
    }

    protected ResultActions get(String path, String token) {
        mockMvc.perform(MockMvcRequestBuilders.get(path)
                                              .headers(headersWithToken(token))
                                              .contentType(APPLICATION_JSON))
    }

    protected ResultActions patch(String endpoint, String requestBody, String token) {
        mockMvc.perform(MockMvcRequestBuilders.patch(endpoint)
                                              .headers(headersWithToken(token))
                                              .content(requestBody)
                                              .contentType(APPLICATION_JSON))
    }

    private HttpHeaders headersWithToken(String token) {
        def headers = new HttpHeaders()
        headers.set(ACCESS_TOKEN_HEADER, token)
        return headers
    }

}
