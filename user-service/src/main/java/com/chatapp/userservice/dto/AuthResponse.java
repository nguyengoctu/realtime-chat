package com.chatapp.userservice.dto;

import lombok.Data;
import lombok.Builder;

/**
 * Data Transfer Object for authentication response containing tokens and user information.
 */
@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private UserResponse user;
}