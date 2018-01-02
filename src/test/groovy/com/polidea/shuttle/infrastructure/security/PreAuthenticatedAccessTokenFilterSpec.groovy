package com.polidea.shuttle.infrastructure.security

import com.polidea.shuttle.error_codes.ForbiddenException
import com.polidea.shuttle.infrastructure.security.authentication.PreAuthenticatedAccessToken
import com.polidea.shuttle.infrastructure.security.authentication.TokenAuthenticationFilter
import org.springframework.security.authentication.AuthenticationManager
import spock.lang.Specification

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class PreAuthenticatedAccessTokenFilterSpec extends Specification {

    static final String ACCESS_TOKEN_HEADER_NAME = "Access-Token"

    static final String UPLOAD_TOKEN_HEADER_NAME = "Upload-Token"

    HttpServletRequest request = Stub(HttpServletRequest)

    HttpServletResponse response = Stub(HttpServletResponse)

    FilterChain filterChain = Mock(FilterChain)

    AuthenticationManager authenticationManager = Mock(AuthenticationManager)

    TokenAuthenticationFilter accessTokenAuthenticationFilter

    void setup() {
        accessTokenAuthenticationFilter = new TokenAuthenticationFilter(authenticationManager)
    }

    def "should throw MissingAccessTokenException when no token is not present in request"() {
        given:
        request.getHeader(ACCESS_TOKEN_HEADER_NAME) >> null
        request.getHeader(UPLOAD_TOKEN_HEADER_NAME) >> null

        when:
        accessTokenAuthenticationFilter.doFilterInternal(request, response, filterChain)

        then:
        def thrownException = thrown(ForbiddenException)
        thrownException.getMessage() == "Request does not contain Access-Token header set"
    }

    def "should use Access-Token header value as access token"() {
        given:
        request.getHeader(ACCESS_TOKEN_HEADER_NAME) >> 'some-access-token'

        when:
        accessTokenAuthenticationFilter.doFilterInternal(request, response, filterChain)

        then:
        1 * authenticationManager.authenticate(new PreAuthenticatedAccessToken('some-access-token'))

        and:
        noExceptionThrown()
    }
}
