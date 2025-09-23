package com.chatapp.userservice.dto;

import com.chatapp.userservice.model.User;
import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for user response containing user information
 * without sensitive data like password hash.
 */
@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private User.UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Creates a UserResponse from a User entity.
     *
     * @param user the User entity to convert
     * @return UserResponse containing user information
     */
    public static UserResponse fromUser(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}