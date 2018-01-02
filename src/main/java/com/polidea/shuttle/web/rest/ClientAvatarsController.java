package com.polidea.shuttle.web.rest;

import com.polidea.shuttle.domain.user.output.DefaultAvatarsResponse;
import com.polidea.shuttle.infrastructure.avatars.DefaultAvatars;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/avatars")
public class ClientAvatarsController {

    private final DefaultAvatars defaultAvatars;

    @Autowired
    public ClientAvatarsController(DefaultAvatars defaultAvatars) {
        this.defaultAvatars = defaultAvatars;
    }

    @RequestMapping(method = GET)
    @ResponseStatus(OK)
    public DefaultAvatarsResponse getDefaultAvatars() {
        return new DefaultAvatarsResponse(defaultAvatars.asList());
    }

}
