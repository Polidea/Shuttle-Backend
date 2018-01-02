package com.polidea.shuttle.configuration

import com.polidea.shuttle.web.rest.HttpIntegrationSpecification

class CorsConfigurationSpec extends HttpIntegrationSpecification {

    private static final String ADMIN_PANEL_DEVELOPMENT_URL = "http://localhost:9876"

    def "request without Origin is allowed"() {
        given:
        def token = createAndAuthenticateUser()

        when:
        def response = jsonHttpClient.performGetRequest(
                serverUrlWith('/profile'),
                ['Access-Token': token]
        )


        then:
        response.code() == 200
    }

    def "request with allowed Origin is allowed"() {
        given:
        def token = createAndAuthenticateUser()

        when:
        def response = jsonHttpClient.performGetRequest(
                serverUrlWith('/profile'),
                ['Access-Token': token,
                 'Origin'      : ADMIN_PANEL_DEVELOPMENT_URL]
        )

        then:
        response.code() == 200
        response.header('Access-Control-Allow-Origin') == ADMIN_PANEL_DEVELOPMENT_URL
    }

    def "request with not allowed Origin is blocked"() {
        given:
        def token = createAndAuthenticateUser()

        when:
        def response = jsonHttpClient.performGetRequest(
                serverUrlWith('/profile'),
                ['Access-Token': token,
                 'Origin'      : 'http://not.allowed.origin']
        )

        then:
        response.code() == 403
    }

    def "pre-flight request with allowed Origin is allowed"() {
        given:
        def token = createAndAuthenticateUser()

        when:
        def response = jsonHttpClient.performOptionsRequest(
                serverUrlWith('/profile'),
                ['Access-Token': token,
                 'Origin'      : ADMIN_PANEL_DEVELOPMENT_URL]
        )

        then:
        response.code() == 200
        response.header('Access-Control-Allow-Origin') == ADMIN_PANEL_DEVELOPMENT_URL
    }

    def "pre-flight request with not allowed Origin is blocked"() {
        given:
        def token = createAndAuthenticateUser()

        when:
        def response = jsonHttpClient.performOptionsRequest(
                serverUrlWith('/profile'),
                ['Access-Token': token,
                 'Origin'      : 'http://not.allowed.origin']
        )

        then:
        response.code() == 403
    }

    def "pre-flight request without Access Token is allowed"() {
        when:
        def response = jsonHttpClient.performOptionsRequest(
                serverUrlWith('/profile'),
                ['Origin': ADMIN_PANEL_DEVELOPMENT_URL]
        )

        then:
        response.code() == 200
    }

    def "pre-flight request with invalid Access Token is allowed"() {
        when:
        def response = jsonHttpClient.performOptionsRequest(
                serverUrlWith('/profile'),
                ['Access-Token': 'any-invalid-token',
                 'Origin'      : ADMIN_PANEL_DEVELOPMENT_URL]
        )

        then:
        response.code() == 200
    }

    private String serverUrlWith(String path) {
        "http://localhost:${serverPort}${path}"
    }

    private String createAndAuthenticateUser() {
        def userEmail = 'any.user@shuttle.com'
        setupHelper.createUser(userEmail, 'Any User', null)
        return setupHelper.createClientAccessToken(userEmail, 'any-device-id')
    }

}
