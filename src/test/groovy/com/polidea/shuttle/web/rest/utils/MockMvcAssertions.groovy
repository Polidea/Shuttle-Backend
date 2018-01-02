package com.polidea.shuttle.web.rest.utils

import org.springframework.test.web.servlet.ResultActions

import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.isEmptyOrNullString
import static org.hamcrest.Matchers.notNullValue
import static org.hamcrest.core.IsNot.not
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

class MockMvcAssertions {

    static ResultActions assertErrorBodyCompletion(ResultActions result, Integer httpStatusCode) {
        result
                .andExpect(jsonPath('$.status', is(httpStatusCode)))
                .andExpect(jsonPath('$.timestamp', not(isEmptyOrNullString())))
                .andExpect(jsonPath('$.message', not(isEmptyOrNullString())))
                .andExpect(jsonPath('$.error', not(isEmptyOrNullString())))
                .andExpect(jsonPath('$.path', is(notNullValue())))
    }

    static ResultActions assertErrorBodyCompletion(ResultActions result, Integer httpStatusCode, Integer code) {
        assertErrorBodyCompletion(result, httpStatusCode)
        .andExpect(jsonPath('$.code', is(code)))
    }
}
