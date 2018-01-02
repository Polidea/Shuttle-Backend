package com.polidea.shuttle.domain.blog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polidea.shuttle.domain.blog.output.BlogListResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class BlogService {

    private final OkHttpClient httpClient = new OkHttpClient();

    public BlogListResponse fetch() {
        Request request = new Request.Builder()
            .url("http://www.polidea.com/blog/feed")
            .build();
        try {
            Response response = httpClient.newCall(request).execute();
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.body().string(), BlogListResponse.class);
        } catch (IOException exception) {
            throw new RuntimeException("Cannot fetch posts from Polidea blog", exception);
        }
    }

}
