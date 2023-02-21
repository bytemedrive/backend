package com.bytemedrive.backend.storage.boundary;


import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


@ApplicationScoped
public class FileStorageFacade {

    @ConfigProperty(name = "aws.bucket.name")
    String awsBucketName;

    @Inject
    S3Client s3Client;


    public void uploadFileToS3(String path, byte[] file) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(awsBucketName)
                        .key(path)
                        .build(),
                RequestBody.fromBytes(file));
    }
}
