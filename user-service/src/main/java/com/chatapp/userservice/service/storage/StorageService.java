package com.chatapp.userservice.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Interface for file storage operations.
 * Provides abstraction for different storage backends (MinIO, S3, etc.).
 */
public interface StorageService {

    /**
     * Uploads a file to the storage backend.
     *
     * @param bucketName the name of the bucket to upload to
     * @param objectName the name of the object (file) in the bucket
     * @param file the multipart file to upload
     * @return the URL or path to access the uploaded file
     * @throws StorageException if upload fails
     */
    String uploadFile(String bucketName, String objectName, MultipartFile file) throws StorageException;

    /**
     * Uploads a file to the storage backend using InputStream.
     *
     * @param bucketName the name of the bucket to upload to
     * @param objectName the name of the object (file) in the bucket
     * @param inputStream the input stream of the file content
     * @param contentType the content type of the file
     * @param size the size of the file in bytes
     * @return the URL or path to access the uploaded file
     * @throws StorageException if upload fails
     */
    String uploadFile(String bucketName, String objectName, InputStream inputStream,
                     String contentType, long size) throws StorageException;

    /**
     * Deletes a file from the storage backend.
     *
     * @param bucketName the name of the bucket containing the file
     * @param objectName the name of the object (file) to delete
     * @throws StorageException if deletion fails
     */
    void deleteFile(String bucketName, String objectName) throws StorageException;

    /**
     * Generates a presigned URL for direct file access.
     *
     * @param bucketName the name of the bucket containing the file
     * @param objectName the name of the object (file)
     * @param expiryInSeconds the expiry time for the URL in seconds
     * @return the presigned URL
     * @throws StorageException if URL generation fails
     */
    String generatePresignedUrl(String bucketName, String objectName, int expiryInSeconds) throws StorageException;

    /**
     * Checks if a bucket exists.
     *
     * @param bucketName the name of the bucket to check
     * @return true if bucket exists, false otherwise
     * @throws StorageException if check fails
     */
    boolean bucketExists(String bucketName) throws StorageException;

    /**
     * Creates a bucket if it doesn't exist.
     *
     * @param bucketName the name of the bucket to create
     * @throws StorageException if bucket creation fails
     */
    void createBucketIfNotExists(String bucketName) throws StorageException;
}