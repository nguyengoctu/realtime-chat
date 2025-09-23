package com.chatapp.userservice.service;

import com.chatapp.userservice.dto.*;
import com.chatapp.userservice.model.RefreshToken;
import com.chatapp.userservice.model.User;
import com.chatapp.userservice.repository.RefreshTokenRepository;
import com.chatapp.userservice.repository.UserRepository;
import com.chatapp.userservice.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private UserRegistrationRequest registrationRequest;
    private UserLoginRequest loginRequest;
    private User testUser;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setUsername("testuser");
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setPassword("password123");
        registrationRequest.setFullName("Test User");

        loginRequest = new UserLoginRequest();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("password123");

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .fullName("Test User")
                .status(User.UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testRefreshToken = RefreshToken.builder()
                .id(1L)
                .token("refresh-token-123")
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void registerUser_Success() {
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registrationRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.registerUser(registrationRequest);

        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getFullName(), result.getFullName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_UsernameExists_ThrowsException() {
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.registerUser(registrationRequest));

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_EmailExists_ThrowsException() {
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.registerUser(registrationRequest));

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginUser_Success() {
        when(userRepository.findByUsername(loginRequest.getUsernameOrEmail()))
                .thenReturn(Optional.of(testUser));
        when(jwtUtil.generateAccessToken(testUser.getUsername())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(testUser.getUsername())).thenReturn("jwt-refresh-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);

        AuthResponse result = userService.loginUser(loginRequest);

        assertNotNull(result);
        assertEquals("access-token", result.getAccessToken());
        assertEquals("jwt-refresh-token", result.getRefreshToken());
        assertEquals(testUser.getUsername(), result.getUser().getUsername());
        verify(refreshTokenRepository).deleteAllByUser(testUser);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(jwtUtil).generateRefreshToken(testUser.getUsername());
    }

    @Test
    void loginUser_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername(loginRequest.getUsernameOrEmail()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail(loginRequest.getUsernameOrEmail()))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.loginUser(loginRequest));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<UserResponse> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        assertEquals(testUser.getEmail(), result.get().getEmail());
    }

    @Test
    void getUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<UserResponse> result = userService.getUserById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void searchUsers_Success() {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.searchUsers("test")).thenReturn(users);

        List<UserResponse> result = userService.searchUsers("test");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getUsername(), result.get(0).getUsername());
    }

    @Test
    void refreshAccessToken_Success() {
        when(jwtUtil.validateToken("refresh-token-123")).thenReturn(true);
        when(jwtUtil.getUsernameFromToken("refresh-token-123")).thenReturn("testuser");
        when(refreshTokenRepository.findByToken("refresh-token-123"))
                .thenReturn(Optional.of(testRefreshToken));
        when(jwtUtil.generateAccessToken(testUser.getUsername())).thenReturn("new-access-token");

        AuthResponse result = userService.refreshAccessToken("refresh-token-123");

        assertNotNull(result);
        assertEquals("new-access-token", result.getAccessToken());
        assertEquals("refresh-token-123", result.getRefreshToken());
        assertEquals(testUser.getUsername(), result.getUser().getUsername());
        verify(jwtUtil).validateToken("refresh-token-123");
        verify(jwtUtil).getUsernameFromToken("refresh-token-123");
    }

    @Test
    void refreshAccessToken_InvalidJwtToken_ThrowsException() {
        when(jwtUtil.validateToken("invalid-jwt-token")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.refreshAccessToken("invalid-jwt-token"));

        assertEquals("Invalid refresh token", exception.getMessage());
        verify(jwtUtil).validateToken("invalid-jwt-token");
        verify(refreshTokenRepository, never()).findByToken(anyString());
    }

    @Test
    void refreshAccessToken_TokenNotFoundInDatabase_ThrowsException() {
        when(jwtUtil.validateToken("valid-jwt-token")).thenReturn(true);
        when(jwtUtil.getUsernameFromToken("valid-jwt-token")).thenReturn("testuser");
        when(refreshTokenRepository.findByToken("valid-jwt-token"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.refreshAccessToken("valid-jwt-token"));

        assertEquals("Refresh token not found", exception.getMessage());
    }

    @Test
    void refreshAccessToken_ExpiredToken_ThrowsException() {
        RefreshToken expiredToken = RefreshToken.builder()
                .token("expired-token")
                .user(testUser)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        when(jwtUtil.validateToken("expired-token")).thenReturn(true);
        when(jwtUtil.getUsernameFromToken("expired-token")).thenReturn("testuser");
        when(refreshTokenRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(expiredToken));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.refreshAccessToken("expired-token"));

        assertEquals("Refresh token expired", exception.getMessage());
        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    void refreshAccessToken_UsernameMismatch_ThrowsException() {
        when(jwtUtil.validateToken("refresh-token-123")).thenReturn(true);
        when(jwtUtil.getUsernameFromToken("refresh-token-123")).thenReturn("differentuser");
        when(refreshTokenRepository.findByToken("refresh-token-123"))
                .thenReturn(Optional.of(testRefreshToken));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.refreshAccessToken("refresh-token-123"));

        assertEquals("Token username mismatch", exception.getMessage());
    }

    @Test
    void revokeRefreshToken_Success() {
        when(jwtUtil.validateToken("refresh-token-123")).thenReturn(true);
        when(refreshTokenRepository.findByToken("refresh-token-123"))
                .thenReturn(Optional.of(testRefreshToken));

        userService.revokeRefreshToken("refresh-token-123");

        verify(jwtUtil).validateToken("refresh-token-123");
        verify(refreshTokenRepository).findByToken("refresh-token-123");
        verify(refreshTokenRepository).delete(testRefreshToken);
    }

    @Test
    void revokeRefreshToken_InvalidJwtToken_ThrowsException() {
        when(jwtUtil.validateToken("invalid-jwt-token")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.revokeRefreshToken("invalid-jwt-token"));

        assertEquals("Invalid refresh token", exception.getMessage());
        verify(jwtUtil).validateToken("invalid-jwt-token");
        verify(refreshTokenRepository, never()).findByToken(anyString());
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void revokeRefreshToken_TokenNotFoundInDatabase_ThrowsException() {
        when(jwtUtil.validateToken("valid-jwt-token")).thenReturn(true);
        when(refreshTokenRepository.findByToken("valid-jwt-token"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.revokeRefreshToken("valid-jwt-token"));

        assertEquals("Refresh token not found", exception.getMessage());
        verify(jwtUtil).validateToken("valid-jwt-token");
        verify(refreshTokenRepository).findByToken("valid-jwt-token");
        verify(refreshTokenRepository, never()).delete(any());
    }
}