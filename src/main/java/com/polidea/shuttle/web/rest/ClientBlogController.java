package com.polidea.shuttle.web.rest;

import com.polidea.shuttle.domain.blog.BlogService;
import com.polidea.shuttle.domain.blog.output.BlogListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/blog")
public class ClientBlogController {

    private final BlogService blogService;

    @Autowired
    public ClientBlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @RequestMapping(method = GET)
    @ResponseStatus(OK)
    public BlogListResponse getBlogPosts() {
        return blogService.fetch();
    }

}
