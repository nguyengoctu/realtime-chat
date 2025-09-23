package com.chatapp.userservice.dto;

import lombok.Data;
import lombok.Builder;

/**
 * Generic API response wrapper containing success status, message, and data.
 *
 * @param <T> the type of data contained in the response
 */
@Data
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    /**
     * Creates a successful API response with data.
     *
     * @param <T> the type of data
     * @param data the response data
     * @return ApiResponse indicating success with data
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * Creates a successful API response with message and data.
     *
     * @param <T> the type of data
     * @param message the success message
     * @param data the response data
     * @return ApiResponse indicating success with message and data
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Creates an error API response with message.
     *
     * @param <T> the type of data
     * @param message the error message
     * @return ApiResponse indicating error with message
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}