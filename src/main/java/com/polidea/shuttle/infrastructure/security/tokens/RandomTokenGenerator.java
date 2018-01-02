package com.polidea.shuttle.infrastructure.security.tokens;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;

public class RandomTokenGenerator {

    private final int length;

    public RandomTokenGenerator(int length) {
        this.length = length;
    }

    public String next() {
        return randomAlphanumeric(length);
    }
}
