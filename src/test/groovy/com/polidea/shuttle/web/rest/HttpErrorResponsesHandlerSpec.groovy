package com.polidea.shuttle.web.rest

import com.polidea.shuttle.error_codes.BadRequestException
import com.polidea.shuttle.error_codes.ConflictException
import com.polidea.shuttle.error_codes.ForbiddenException
import com.polidea.shuttle.error_codes.UnauthorizedException
import com.polidea.shuttle.infrastructure.mail.UnableToGenerateQRCodeException
import com.polidea.shuttle.web.rest.utils.mockmvc.ControllerAdviceMock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Unroll

import javax.persistence.EntityNotFoundException

import static com.polidea.shuttle.TestConstants.TEST_TOKEN
import static com.polidea.shuttle.web.rest.utils.MockMvcAssertions.assertErrorBodyCompletion

class HttpErrorResponsesHandlerSpec extends MockMvcIntegrationSpecification {

    ControllerAdviceMock adviceTestingController = Mock(ControllerAdviceMock)

    @Autowired
    HttpErrorResponsesHandler errorResponsesHandler

    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(adviceTestingController)
                                 .setControllerAdvice(errorResponsesHandler)
                                 .build()
    }

    @Unroll
    def "should return #errorCode on #exception.getClass().getName()"(Exception exception, Integer errorCode) {
        given:
        adviceTestingController.simulateGetMethod() >> { throw exception }

        when:
        ResultActions result = get('/errorCodesTest', TEST_TOKEN)

        then:
        assertErrorBodyCompletion(result, errorCode)

        where:
        exception                                          | errorCode
        new BadRequestException('any-message')             | 400
        new UnauthorizedException('any-message')           | 401
        new ForbiddenException('any-message')              | 403
        new EntityNotFoundException('any-message')         | 404
        new ConflictException('any-message')               | 409
        new DataIntegrityViolationException('any-message') | 409
        new UnableToGenerateQRCodeException('any-message') | 409
    }
}
