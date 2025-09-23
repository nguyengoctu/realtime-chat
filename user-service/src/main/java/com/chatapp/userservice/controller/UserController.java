package com.chatapp.userservice.controller;

import com.chatapp.userservice.dto.*;
import com.chatapp.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for user-related operations including registration, authentication,
 * user retrieval, and search functionality.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Registers a new user in the system.
     *
     * @param request the user registration request containing username, email, password, and full name
     * @return ResponseEntity containing the API response with user data or error message
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            UserResponse userResponse = userService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User registered successfully", userResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Authenticates a user and returns access and refresh tokens.
     *
     * @param request the login request containing username/email and password
     * @return ResponseEntity containing the API response with authentication tokens and user data
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginUser(@Valid @RequestBody UserLoginRequest request) {
        try {
            AuthResponse authResponse = userService.loginUser(request);
            return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param id the unique identifier of the user
     * @return ResponseEntity containing the API response with user data or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(ApiResponse.success(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Searches for users based on a keyword matching username, email, or full name.
     *
     * @param keyword the search keyword to match against user fields
     * @return ResponseEntity containing the API response with list of matching users
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(@RequestParam String keyword) {
        List<UserResponse> users = userService.searchUsers(keyword);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Refreshes an access token using a valid refresh token.
     *
     * @param refreshToken the refresh token to generate a new access token
     * @return ResponseEntity containing the API response with new authentication tokens
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            AuthResponse authResponse = userService.refreshAccessToken(request.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", authResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Logs out a user by revoking their refresh token.
     *
     * @param refreshToken the refresh token to revoke
     * @return ResponseEntity containing the API response with logout confirmation
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            userService.revokeRefreshToken(request.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}