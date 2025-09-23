package com.chatapp.userservice.controller;

import com.chatapp.userservice.dto.ApiResponse;
import com.chatapp.userservice.service.storage.StorageService;
import com.chatapp.userservice.service.storage.StorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for file upload operations, particularly for avatar images.
 * Supports MinIO storage backend with easy switching to S3 later.
 */
@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {

    @Autowired
    private StorageService storageService;

    @Value("${MINIO_BUCKET_AVATARS:avatars}")
    private String avatarsBucket;

    // Allowed image file types
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    // Maximum file size: 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * Uploads an avatar image file.
     *
     * @param file the avatar image file to upload
     * @return ResponseEntity containing the API response with file URL or error message
     */
    @PostMapping("/avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            String validationError = validateImageFile(file);
            if (validationError != null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(validationError));
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String objectName = UUID.randomUUID().toString() + fileExtension;

            // Upload file
            storageService.uploadFile(
                    avatarsBucket,
                    objectName,
                    file
            );

            // Return only relative path, frontend will construct full URL
            String relativePath = "/storage/" + avatarsBucket + "/" + objectName;
            return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", relativePath));

        } catch (StorageException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Upload failed: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Unexpected error occurred during upload"));
        }
    }

    /**
     * Deletes an avatar file from storage.
     *
     * @param objectName the name of the object to delete (relative path from bucket root)
     * @return ResponseEntity containing the API response with deletion confirmation
     */
    @DeleteMapping("/avatar")
    public ResponseEntity<ApiResponse<String>> deleteAvatar(@RequestParam("objectName") String objectName) {
        try {
            storageService.deleteFile(avatarsBucket, objectName);
            return ResponseEntity.ok(ApiResponse.success("File deleted successfully"));
        } catch (StorageException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Deletion failed: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Unexpected error occurred during deletion"));
        }
    }

    /**
     * Generates a presigned URL for direct file access.
     *
     * @param objectName the name of the object to generate URL for
     * @param expiryInSeconds the expiry time for the URL in seconds (default: 3600)
     * @return ResponseEntity containing the API response with presigned URL
     */
    @GetMapping("/avatar/presigned-url")
    public ResponseEntity<ApiResponse<String>> generatePresignedUrl(
            @RequestParam("objectName") String objectName,
            @RequestParam(value = "expiryInSeconds", defaultValue = "3600") int expiryInSeconds) {
        try {
            String presignedUrl = storageService.generatePresignedUrl(
                    avatarsBucket,
                    objectName,
                    expiryInSeconds
            );
            return ResponseEntity.ok(ApiResponse.success("Presigned URL generated successfully", presignedUrl));
        } catch (StorageException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to generate presigned URL: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Unexpected error occurred while generating presigned URL"));
        }
    }

    /**
     * Validates an uploaded image file.
     *
     * @param file the file to validate
     * @return error message if validation fails, null if validation passes
     */
    private String validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "File is required and cannot be empty";
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return "File size exceeds maximum limit of 5MB";
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            return "Invalid file type. Only JPEG, PNG, GIF, and WebP images are allowed";
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return "File must have a valid filename";
        }

        return null; // Validation passed
    }

    /**
     * Extracts file extension from filename.
     *
     * @param filename the filename to extract extension from
     * @return the file extension including the dot (e.g., ".jpg")
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
}