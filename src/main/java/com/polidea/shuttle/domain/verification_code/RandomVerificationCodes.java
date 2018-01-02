package com.polidea.shuttle.domain.verification_code;

import org.springframework.stereotype.Component;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;

@Component
public class RandomVerificationCodes {

    private static final int VERIFICATION_CODE_LENGTH = 5;

    public String next() {
        return randomAlphabetic(VERIFICATION_CODE_LENGTH).toUpperCase();
    }
}
