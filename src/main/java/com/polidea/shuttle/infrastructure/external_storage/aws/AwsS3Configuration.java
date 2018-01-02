package com.polidea.shuttle.infrastructure.external_storage.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@SuppressWarnings("unused")
class AwsS3Configuration {

    @Value("${aws.s3.access-key}")
    private String accessKey;
    @Value("${aws.s3.secret-access-key}")
    private String secretAccessKey;
    @Value("${aws.s3.region}")
    private String region;

    @Bean
    public AWSCredentials awsCredentials() {
        return new BasicAWSCredentials(accessKey, secretAccessKey);
    }

    @Bean
    public ClientConfiguration clientConfiguration() {
        return new ClientConfiguration();
    }

    @Bean
    public AmazonS3 amazonS3(AWSCredentials awsCredentials, ClientConfiguration clientConfiguration) {
        AmazonS3 amazonS3 = new AmazonS3Client(awsCredentials, clientConfiguration);
        amazonS3.setRegion(Region.getRegion(Regions.fromName(region)));
        return amazonS3;
    }

}
