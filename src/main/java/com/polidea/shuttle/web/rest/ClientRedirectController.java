package com.polidea.shuttle.web.rest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@SuppressWarnings("unused")
@Controller
public class ClientRedirectController {

    public static final String AUTH_REDIRECT_PATH = "/auth/redirect";
    public static final String DOWNLOAD_REDIRECT_PATH = "/download/redirect";

    @RequestMapping(method = GET, value = AUTH_REDIRECT_PATH)
    public String getAuthRedirectPage(@RequestParam(required = true) String email,
                                      @RequestParam(required = true) String code,
                                      Model redirectPageAttributes) {
        redirectPageAttributes.addAttribute("email", email);
        redirectPageAttributes.addAttribute("code", code);
        return "auth_redirect";
    }

    @RequestMapping(method = GET, value = DOWNLOAD_REDIRECT_PATH)
    public String getDownloadRedirectPage(@RequestParam(required = true) String url,
                                          Model redirectPageAttributes) {
        redirectPageAttributes.addAttribute("url", url);
        return "download_redirect";
    }

}

