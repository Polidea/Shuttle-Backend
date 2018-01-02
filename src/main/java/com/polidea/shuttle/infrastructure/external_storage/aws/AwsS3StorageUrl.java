package com.polidea.shuttle.infrastructure.external_storage.aws;

import com.polidea.shuttle.infrastructure.external_storage.ExternalStorageUrl;

import static java.lang.String.format;

class AwsS3StorageUrl implements ExternalStorageUrl {

    private final String resourceKey;
    private final String baseUrl;

    AwsS3StorageUrl(String baseUrl, String resourceKey) {
        this.baseUrl = baseUrl;
        this.resourceKey = resourceKey;
    }

    @Override
    public String asText() {
        return format("%s/%s", baseUrl, resourceKey);
    }

}
