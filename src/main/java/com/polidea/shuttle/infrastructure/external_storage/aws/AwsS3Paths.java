package com.polidea.shuttle.infrastructure.external_storage.aws;

import com.polidea.shuttle.infrastructure.external_storage.ExternalStoragePaths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

@Service
@SuppressWarnings("unused")
class AwsS3Paths implements ExternalStoragePaths {

    @Value("${aws.s3.base-paths.avatars.uploaded}")
    private String avatarsBasePath;

    @Override
    public String pathForAvatarIdentifiedBy(String uniqueIdentifier) {
        return format("%s/avatar_%s", avatarsBasePath, uniqueIdentifier);
    }

}
