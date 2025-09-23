package com.chatapp.userservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data transfer object for refresh token requests.
 * Contains the refresh token to be validated and used for generating new access tokens.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    /**
     * The refresh token to be validated and used for token refresh.
     */
    private String refreshToken;
}