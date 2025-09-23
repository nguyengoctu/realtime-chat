package com.chatapp.userservice.service;

import com.chatapp.common.annotation.ReadOnlyRepository;
import com.chatapp.common.annotation.WriteRepository;
import com.chatapp.userservice.dto.*;
import com.chatapp.userservice.model.RefreshToken;
import com.chatapp.userservice.model.User;
import com.chatapp.userservice.repository.RefreshTokenRepository;
import com.chatapp.userservice.repository.UserRepository;
import com.chatapp.userservice.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for user management operations including registration, authentication,
 * user retrieval, search, and token management.
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;


    /**
     * Registers a new user in the system after validating uniqueness of username and email.
     *
     * @param request the registration request containing user details
     * @return UserResponse containing the registered user's information
     * @throws RuntimeException if username or email already exists
     */
    @WriteRepository
    public UserResponse registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .status(User.UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);
        return UserResponse.fromUser(savedUser);
    }

    /**
     * Authenticates a user and generates access and refresh tokens.
     *
     * @param request the login request containing username/email and password
     * @return AuthResponse containing authentication tokens and user information
     * @throws RuntimeException if authentication fails or user not found
     */
    @WriteRepository
    public AuthResponse loginUser(UserLoginRequest request) {
        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .orElseGet(() -> userRepository.findByEmail(request.getUsernameOrEmail())
                        .orElseThrow(() -> new RuntimeException("User not found")));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserResponse.fromUser(user))
                .build();
    }

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param id the unique identifier of the user
     * @return Optional containing UserResponse if user exists, empty otherwise
     */
    @ReadOnlyRepository
    public Optional<UserResponse> getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::fromUser);
    }

    /**
     * Searches for users based on a keyword matching username, email, or full name.
     *
     * @param keyword the search keyword
     * @return List of UserResponse objects matching the search criteria
     */
    @ReadOnlyRepository
    public List<UserResponse> searchUsers(String keyword) {
        return userRepository.searchUsers(keyword)
                .stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new refresh token for the specified user, removing any existing tokens.
     *
     * @param user the user to create a refresh token for
     * @return the generated refresh token string
     */
    private String createRefreshToken(User user) {
        refreshTokenRepository.deleteAllByUser(user);

        String token = jwtUtil.generateRefreshToken(user.getUsername());
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        refreshTokenRepository.save(refreshToken);
        return token;
    }

    /**
     * Refreshes an access token using a valid refresh token.
     *
     * @param refreshToken the refresh token to validate and use for generating new access token
     * @return AuthResponse containing the new access token and user information
     * @throws RuntimeException if refresh token is invalid, expired, or not found
     */
    @WriteRepository
    public AuthResponse refreshAccessToken(String refreshToken) {
        // Validate JWT refresh token first
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // Get username from JWT
        String username = jwtUtil.getUsernameFromToken(refreshToken);

        // Find token in database
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        // Check if token is expired in database
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired");
        }

        // Verify username matches
        if (!token.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Token username mismatch");
        }

        String newAccessToken = jwtUtil.generateAccessToken(username);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .user(UserResponse.fromUser(token.getUser()))
                .build();
    }

    /**
     * Revokes a refresh token by removing it from the database.
     *
     * @param refreshToken the refresh token to revoke
     * @throws RuntimeException if refresh token is invalid or not found
     */
    @WriteRepository
    public void revokeRefreshToken(String refreshToken) {
        // Validate JWT refresh token first
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // Find and delete token from database
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        refreshTokenRepository.delete(token);
    }
}