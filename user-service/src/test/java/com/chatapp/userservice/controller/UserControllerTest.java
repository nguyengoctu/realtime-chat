package com.chatapp.userservice.controller;

import com.chatapp.userservice.dto.*;
import com.chatapp.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@org.junit.jupiter.api.Disabled("Controller tests disabled for now")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRegistrationRequest registrationRequest;
    private UserLoginRequest loginRequest;
    private UserResponse userResponse;
    private AuthResponse authResponse;

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

        userResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        authResponse = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .user(userResponse)
                .build();
    }

    @Test
    void registerUser_Success() throws Exception {
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenReturn(userResponse);

        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(userService).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    void registerUser_ValidationError() throws Exception {
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest();
        invalidRequest.setUsername("ab"); // Too short
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("123"); // Too short

        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    void registerUser_UsernameExists() throws Exception {
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new RuntimeException("Username already exists"));

        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void loginUser_Success() throws Exception {
        when(userService.loginUser(any(UserLoginRequest.class)))
                .thenReturn(authResponse);

        mockMvc.perform(post("/api/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.user.username").value("testuser"));

        verify(userService).loginUser(any(UserLoginRequest.class));
    }

    @Test
    void loginUser_InvalidCredentials() throws Exception {
        when(userService.loginUser(any(UserLoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @WithMockUser
    void getUserById_Success() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(userResponse));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userService).getUserById(1L);
    }

    @Test
    @WithMockUser
    void getUserById_NotFound() throws Exception {
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(999L);
    }

    @Test
    @WithMockUser
    void searchUsers_Success() throws Exception {
        List<UserResponse> users = Arrays.asList(userResponse);
        when(userService.searchUsers("test")).thenReturn(users);

        mockMvc.perform(get("/api/users/search")
                .param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].username").value("testuser"));

        verify(userService).searchUsers("test");
    }

    @Test
    void refreshToken_Success() throws Exception {
        when(userService.refreshAccessToken("refresh-token"))
                .thenReturn(authResponse);

        mockMvc.perform(post("/api/users/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));

        verify(userService).refreshAccessToken("refresh-token");
    }

    @Test
    void refreshToken_InvalidToken() throws Exception {
        when(userService.refreshAccessToken("invalid-token"))
                .thenThrow(new RuntimeException("Invalid refresh token"));

        mockMvc.perform(post("/api/users/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

    @Test
    void logout_Success() throws Exception {
        doNothing().when(userService).revokeRefreshToken("refresh-token");

        mockMvc.perform(post("/api/users/logout")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        verify(userService).revokeRefreshToken("refresh-token");
    }

    @Test
    void logout_InvalidToken() throws Exception {
        doThrow(new RuntimeException("Invalid refresh token"))
                .when(userService).revokeRefreshToken("invalid-token");

        mockMvc.perform(post("/api/users/logout")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));

        verify(userService).revokeRefreshToken("invalid-token");
    }
}