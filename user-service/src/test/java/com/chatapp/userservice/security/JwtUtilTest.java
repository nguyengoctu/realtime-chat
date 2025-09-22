package com.chatapp.userservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", "mySecretKeyForTestingPurposesOnly1234567890");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 900000); // 15 minutes
        ReflectionTestUtils.setField(jwtUtil, "jwtRefreshExpirationMs", 604800000); // 7 days
    }

    @Test
    void generateAccessToken_Success() {
        String token = jwtUtil.generateAccessToken("testuser");

        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertEquals("testuser", jwtUtil.getUsernameFromToken(token));
    }

    @Test
    void generateRefreshToken_Success() {
        String token = jwtUtil.generateRefreshToken("testuser");

        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertEquals("testuser", jwtUtil.getUsernameFromToken(token));
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        String token = jwtUtil.generateAccessToken("testuser");

        boolean isValid = jwtUtil.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        String invalidToken = "invalid.jwt.token";

        boolean isValid = jwtUtil.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void validateToken_ExpiredToken_ReturnsFalse() {
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", -1000);
        String expiredToken = jwtUtil.generateAccessToken("testuser");

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean isValid = jwtUtil.validateToken(expiredToken);

        assertFalse(isValid);
    }

    @Test
    void getUsernameFromToken_ValidToken_ReturnsUsername() {
        String token = jwtUtil.generateAccessToken("testuser");

        String username = jwtUtil.getUsernameFromToken(token);

        assertEquals("testuser", username);
    }

    @Test
    void getExpirationDateFromToken_ValidToken_ReturnsExpirationDate() {
        String token = jwtUtil.generateAccessToken("testuser");

        Date expirationDate = jwtUtil.getExpirationDateFromToken(token);

        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    void accessTokenExpiration_ShorterThanRefreshToken() {
        String accessToken = jwtUtil.generateAccessToken("testuser");
        String refreshToken = jwtUtil.generateRefreshToken("testuser");

        Date accessExpiration = jwtUtil.getExpirationDateFromToken(accessToken);
        Date refreshExpiration = jwtUtil.getExpirationDateFromToken(refreshToken);

        assertTrue(accessExpiration.before(refreshExpiration));
    }
}