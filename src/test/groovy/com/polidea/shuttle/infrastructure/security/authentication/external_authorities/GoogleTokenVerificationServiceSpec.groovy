package com.polidea.shuttle.infrastructure.security.authentication.external_authorities

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.json.webtoken.JsonWebSignature
import spock.lang.Specification
import spock.lang.Unroll

import static com.polidea.shuttle.TestConstants.TEST_EMAIL
import static com.polidea.shuttle.TestConstants.TEST_TOKEN

class GoogleTokenVerificationServiceSpec extends Specification {

    static final String AUDIENCE = "audience"

    static final byte[] notNullByteSignature = "".getBytes()

    GoogleIdTokenVerifier googleIdTokenVerifier = Mock(GoogleIdTokenVerifier)

    GoogleTokenVerificationService googleTokenVerificationService

    void setup() {
        googleTokenVerificationService = new GoogleTokenVerificationService(googleIdTokenVerifier)
        googleTokenVerificationService.googleApplicationClientId = AUDIENCE
    }

    def "should return token owner email while token is valid"() {
        given:
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload()
        payload.setEmail(TEST_EMAIL)
        payload.setAudience(AUDIENCE)

        when:
        Optional owner = googleTokenVerificationService.verifyToken(TEST_TOKEN)

        then:
        1 * googleIdTokenVerifier.verify(TEST_TOKEN) >> new GoogleIdToken(new JsonWebSignature.Header(), payload, this.notNullByteSignature, this.notNullByteSignature)

        and:
        owner.get() == TEST_EMAIL
    }

    def "should return empty Optional if audience is not valid"() {
        given:
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload()
        payload.setEmail(TEST_EMAIL)
        payload.setAudience("invalidAudience")

        when:
        Optional owner = googleTokenVerificationService.verifyToken(TEST_TOKEN)

        then:
        1 * googleIdTokenVerifier.verify(TEST_TOKEN) >> new GoogleIdToken(new JsonWebSignature.Header(), payload, this.notNullByteSignature, this.notNullByteSignature)

        and:
        !owner.isPresent()
    }

    def "should return empty optional if token is invalid"() {
        when:
        Optional owner = googleTokenVerificationService.verifyToken(TEST_TOKEN)

        then:
        1 * googleIdTokenVerifier.verify(TEST_TOKEN) >> new GoogleIdToken(new JsonWebSignature.Header(), new GoogleIdToken.Payload(), this.notNullByteSignature, this.notNullByteSignature)

        and:
        !owner.isPresent()
    }

    def "should return empty optional if GeneralSecurityException is thrown"() {
        when:
        Optional owner = googleTokenVerificationService.verifyToken(TEST_TOKEN)

        then:
        1 * googleIdTokenVerifier.verify(TEST_TOKEN) >> new GoogleIdToken(new JsonWebSignature.Header(), new GoogleIdToken.Payload(), this.notNullByteSignature, this.notNullByteSignature)

        and:
        !owner.isPresent()
    }

    @Unroll
    def "should return empty optional if given token has invalid value"(String tokenValue) {
        when:
        Optional owner = googleTokenVerificationService.verifyToken(tokenValue)

        then:
        0 * googleIdTokenVerifier.verify(_)

        and:
        !owner.isPresent()

        where:
        tokenValue << [null, ""]
    }
}
