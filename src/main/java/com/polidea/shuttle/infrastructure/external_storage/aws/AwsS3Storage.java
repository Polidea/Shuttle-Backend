package com.polidea.shuttle.infrastructure.external_storage.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.polidea.shuttle.infrastructure.external_storage.ExternalStorage;
import com.polidea.shuttle.infrastructure.external_storage.ExternalStorageUrl;
import com.polidea.shuttle.infrastructure.external_storage.UploadToExternalStorageFailedException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

@Service
class AwsS3Storage implements ExternalStorage {

    private final static Logger LOGGER = getLogger(AwsS3Storage.class);

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.region}")
    private String regionName;
    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    @Value("${aws.s3.base-url}")
    private String baseUrl;

    @Autowired
    public AwsS3Storage(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    // Implementation based on...
    // ... https://github.com/brant-hwang/spring-cloud-aws-example/blob/master/src/main/java/com/axisj/spring/cloud/aws/s3/S3Wrapper.java
    @Override
    public ExternalStorageUrl uploadFile(MultipartFile multipartFile, String resourcePathToSet, String contentType) {

        LOGGER.info(format(
            "Uploading file '%s' of Content-Type '%s' to AWS S3 (region: '%s', bucket: '%s', resourceKey: '%s'",
            multipartFile.getOriginalFilename(),
            multipartFile.getContentType(),
            regionName,
            bucketName,
            resourcePathToSet
        ));

        try (InputStream inputStream = multipartFile.getInputStream()) {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            // AWS S3 has to know that it's providing an image. But we cannot tell whether it is...
            // ... PNG or JPEG ot something different because we cannot assure that mobile app...
            // ... will provide correct Content-Type of file provided in form-data part.
            objectMetadata.setContentType(contentType);
            amazonS3.putObject(
                bucketName,
                resourcePathToSet,
                inputStream,
                objectMetadata
            );
        } catch (Exception exception) {
            throw new UploadToExternalStorageFailedException();
        }

        return new AwsS3StorageUrl(baseUrl, resourcePathToSet);
    }

}
