package com.chatapp.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtil class.
 * Tests JWT token validation, username extraction, and expiration checking.
 */
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String testSecret = "mySecretKey123456789012345678901234567890";
    private final String testUsername = "testuser";

    /**
     * Set up test environment before each test.
     */
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", testSecret);
    }

    /**
     * Test valid JWT token validation.
     * Verifies that a properly formed, non-expired token is considered valid.
     */
    @Test
    void shouldValidateValidToken() {
        // Given
        String validToken = createValidToken(testUsername, 3600000); // 1 hour from now

        // When
        boolean isValid = jwtUtil.validateToken(validToken);

        // Then
        assertTrue(isValid);
    }

    /**
     * Test invalid JWT token validation.
     * Verifies that malformed tokens are rejected.
     */
    @Test
    void shouldRejectInvalidToken() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    /**
     * Test expired JWT token validation.
     * Verifies that expired tokens are rejected.
     */
    @Test
    void shouldRejectExpiredToken() {
        // Given
        String expiredToken = createValidToken(testUsername, -3600000); // 1 hour ago

        // When
        boolean isValid = jwtUtil.validateToken(expiredToken);

        // Then
        assertFalse(isValid);
    }

    /**
     * Test username extraction from valid JWT token.
     * Verifies that the correct username is extracted from a valid token.
     */
    @Test
    void shouldExtractUsernameFromValidToken() {
        // Given
        String validToken = createValidToken(testUsername, 3600000);

        // When
        String extractedUsername = jwtUtil.getUsernameFromToken(validToken);

        // Then
        assertEquals(testUsername, extractedUsername);
    }

    /**
     * Test username extraction from invalid JWT token.
     * Verifies that an exception is thrown when trying to extract username from invalid token.
     */
    @Test
    void shouldThrowExceptionWhenExtractingUsernameFromInvalidToken() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When & Then
        assertThrows(Exception.class, () -> jwtUtil.getUsernameFromToken(invalidToken));
    }

    /**
     * Test token expiration check for expired token.
     * Verifies that expired tokens are correctly identified.
     */
    @Test
    void shouldIdentifyExpiredToken() {
        // Given
        String expiredToken = createValidToken(testUsername, -3600000); // 1 hour ago

        // When
        boolean isExpired = jwtUtil.isTokenExpired(expiredToken);

        // Then
        assertTrue(isExpired);
    }

    /**
     * Test token expiration check for valid token.
     * Verifies that non-expired tokens are correctly identified.
     */
    @Test
    void shouldIdentifyNonExpiredToken() {
        // Given
        String validToken = createValidToken(testUsername, 3600000); // 1 hour from now

        // When
        boolean isExpired = jwtUtil.isTokenExpired(validToken);

        // Then
        assertFalse(isExpired);
    }

    /**
     * Test claims extraction from valid JWT token.
     * Verifies that claims can be extracted from a valid token.
     */
    @Test
    void shouldExtractClaimsFromValidToken() {
        // Given
        String validToken = createValidToken(testUsername, 3600000);

        // When
        var claims = jwtUtil.getClaims(validToken);

        // Then
        assertNotNull(claims);
        assertEquals(testUsername, claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    /**
     * Test claims extraction from invalid JWT token.
     * Verifies that an exception is thrown when trying to extract claims from invalid token.
     */
    @Test
    void shouldThrowExceptionWhenExtractingClaimsFromInvalidToken() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When & Then
        assertThrows(Exception.class, () -> jwtUtil.getClaims(invalidToken));
    }

    /**
     * Helper method to create a valid JWT token for testing.
     *
     * @param username the username to include in the token
     * @param expirationOffset offset in milliseconds from current time for expiration
     * @return a valid JWT token string
     */
    private String createValidToken(String username, long expirationOffset) {
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes());
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationOffset))
                .signWith(key)
                .compact();
    }
}