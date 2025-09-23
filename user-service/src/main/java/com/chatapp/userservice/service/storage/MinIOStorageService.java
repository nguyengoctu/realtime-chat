package com.chatapp.userservice.service.storage;

import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * MinIO implementation of the StorageService interface.
 * Provides file storage operations using MinIO object storage.
 */
@Component
public class MinIOStorageService implements StorageService {

    @Value("${MINIO_ENDPOINT:http://localhost:9000}")
    private String endpoint;

    @Value("${MINIO_ACCESS_KEY:minioadmin}")
    private String accessKey;

    @Value("${MINIO_SECRET_KEY:minioadmin}")
    private String secretKey;

    @Value("${MINIO_BUCKET_AVATARS:avatars}")
    private String avatarsBucket;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Override
    public String uploadFile(String bucketName, String objectName, MultipartFile file) throws StorageException {
        try {
            createBucketIfNotExists(bucketName);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // Return the URL to access the uploaded file
            return String.format("%s/%s/%s", endpoint, bucketName, objectName);
        } catch (Exception e) {
            throw new StorageException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @Override
    public String uploadFile(String bucketName, String objectName, InputStream inputStream,
                           String contentType, long size) throws StorageException {
        try {
            createBucketIfNotExists(bucketName);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );

            // Return the URL to access the uploaded file
            return String.format("%s/%s/%s", endpoint, bucketName, objectName);
        } catch (Exception e) {
            throw new StorageException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String bucketName, String objectName) throws StorageException {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    @Override
    public String generatePresignedUrl(String bucketName, String objectName, int expiryInSeconds) throws StorageException {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiryInSeconds, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Failed to generate presigned URL: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean bucketExists(String bucketName) throws StorageException {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new StorageException("Failed to check bucket existence: " + e.getMessage(), e);
        }
    }

    @Override
    public void createBucketIfNotExists(String bucketName) throws StorageException {
        try {
            if (!bucketExists(bucketName)) {
                // Create bucket
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());

                // Set public read policy
                String policy = String.format("""
                {
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                            "Effect": "Allow",
                            "Principal": {"AWS": "*"},
                            "Action": "s3:GetObject",
                            "Resource": "arn:aws:s3:::%s/*"
                        }
                    ]
                }
                """, bucketName);

                minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(policy)
                        .build()
                );
            }
        } catch (Exception e) {
            throw new StorageException("Failed to create bucket: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the configured avatars bucket name.
     *
     * @return the avatars bucket name
     */
    public String getAvatarsBucket() {
        return avatarsBucket;
    }
}