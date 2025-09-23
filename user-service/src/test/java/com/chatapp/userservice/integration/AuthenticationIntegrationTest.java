package com.chatapp.userservice.integration;

import com.chatapp.userservice.dto.UserRegistrationRequest;
import com.chatapp.userservice.dto.UserLoginRequest;
import com.chatapp.userservice.model.User;
import com.chatapp.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@org.junit.jupiter.api.Disabled("Integration tests disabled for now")
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRegistrationRequest registrationRequest;
    private UserLoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setUsername("testuser");
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setPassword("password123");
        registrationRequest.setFullName("Test User");

        loginRequest = new UserLoginRequest();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("password123");

        testUser = User.builder()
                .username("existinguser")
                .email("existing@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .fullName("Existing User")
                .status(User.UserStatus.ACTIVE)
                .build();
    }

    @Test
    void fullAuthenticationFlow_Success() throws Exception {
        // Register new user
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        // Login with registered user
        String loginResponse = mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract tokens for further use
        // In a real test, you would parse the response and extract tokens
    }

    @Test
    void registerUser_DuplicateUsername_Fails() throws Exception {
        userRepository.save(testUser);

        UserRegistrationRequest duplicateRequest = new UserRegistrationRequest();
        duplicateRequest.setUsername("existinguser");
        duplicateRequest.setEmail("new@example.com");
        duplicateRequest.setPassword("password123");
        duplicateRequest.setFullName("New User");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void registerUser_DuplicateEmail_Fails() throws Exception {
        userRepository.save(testUser);

        UserRegistrationRequest duplicateRequest = new UserRegistrationRequest();
        duplicateRequest.setUsername("newuser");
        duplicateRequest.setEmail("existing@example.com");
        duplicateRequest.setPassword("password123");
        duplicateRequest.setFullName("New User");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    void loginUser_WithEmail_Success() throws Exception {
        userRepository.save(testUser);

        UserLoginRequest emailLoginRequest = new UserLoginRequest();
        emailLoginRequest.setUsernameOrEmail("existing@example.com");
        emailLoginRequest.setPassword("password123");

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.username").value("existinguser"));
    }

    @Test
    void loginUser_InvalidCredentials_Fails() throws Exception {
        userRepository.save(testUser);

        UserLoginRequest invalidRequest = new UserLoginRequest();
        invalidRequest.setUsernameOrEmail("existinguser");
        invalidRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}