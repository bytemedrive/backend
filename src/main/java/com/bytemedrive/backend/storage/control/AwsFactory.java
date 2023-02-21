package com.bytemedrive.backend.storage.control;

import io.quarkus.runtime.Startup;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;


//@Startup
//@ApplicationScoped
public class AwsFactory {

    @ConfigProperty(name = "aws.bucket.region")
    String awsBucketRegion;

    @ConfigProperty(name = "aws.access-key")
    String awsAccessKey;

    @ConfigProperty(name = "aws.secret-key")
    String awsSecretKey;

    S3Client amazonS3Client;

    @PostConstruct
    void init() {
        amazonS3Client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(awsAccessKey, awsSecretKey)))
                .region(Region.of(awsBucketRegion))
                .build();
    }

    @Produces
    public S3Client getS3Client() {
        return amazonS3Client;
    }

}
