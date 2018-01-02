package com.polidea.shuttle.domain.app;

import static com.google.common.base.Preconditions.checkNotNull;

public enum Platform {
    IOS,
    ANDROID;

    public static Platform determinePlatformFromText(String platformAsText) {
        try {
            checkNotNull(platformAsText, "'platform' must not be null");
            return Platform.valueOf(platformAsText.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidPlatformException(platformAsText);
        }
    }
}
