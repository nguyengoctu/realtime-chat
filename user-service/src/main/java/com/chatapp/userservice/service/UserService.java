package com.chatapp.userservice.service;

import com.chatapp.userservice.dto.*;
import com.chatapp.userservice.model.RefreshToken;
import com.chatapp.userservice.model.User;
import com.chatapp.userservice.repository.RefreshTokenRepository;
import com.chatapp.userservice.repository.UserRepository;
import com.chatapp.userservice.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Autowired
    private AuthenticationManager authenticationManager;

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

    public AuthResponse loginUser(UserLoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .orElseGet(() -> userRepository.findByEmail(request.getUsernameOrEmail())
                        .orElseThrow(() -> new RuntimeException("User not found")));

        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserResponse.fromUser(user))
                .build();
    }

    public Optional<UserResponse> getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::fromUser);
    }

    public List<UserResponse> searchUsers(String keyword) {
        return userRepository.searchUsers(keyword)
                .stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }

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
}