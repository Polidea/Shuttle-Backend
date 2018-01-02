package com.polidea.shuttle.web.rest.utils.mockmvc

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import static org.springframework.web.bind.annotation.RequestMethod.GET

@RestController("/errorCodesTest")
class ControllerAdviceMock {

    @RequestMapping(method = GET)
    void simulateGetMethod() {
    }

}
