package com.polidea.shuttle.infrastructure.avatars;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

@Service
public class DefaultAvatars {

    private final Random random = new Random();

    @Value("${shuttle.assets.avatars.default[0]}")
    private String url1;
    @Value("${shuttle.assets.avatars.default[1]}")
    private String url2;
    @Value("${shuttle.assets.avatars.default[2]}")
    private String url3;
    @Value("${shuttle.assets.avatars.default[3]}")
    private String url4;
    @Value("${shuttle.assets.avatars.default[4]}")
    private String url5;
    @Value("${shuttle.assets.avatars.default[5]}")
    private String url6;
    @Value("${shuttle.assets.avatars.default[6]}")
    private String url7;
    @Value("${shuttle.assets.avatars.default[7]}")
    private String url8;
    @Value("${shuttle.assets.avatars.default[8]}")
    private String url9;

    public List<Avatar> asList() {
        return newArrayList(
            url1,
            url2,
            url3,
            url4,
            url5,
            url6,
            url7,
            url8,
            url9
        ).stream()
         .map(url -> new Avatar(url))
         .collect(toList());
    }

    public Avatar random() {
        List<Avatar> allAvatars = asList();
        int randomIndex = random.nextInt(allAvatars.size());
        return allAvatars.get(randomIndex);
    }

}
