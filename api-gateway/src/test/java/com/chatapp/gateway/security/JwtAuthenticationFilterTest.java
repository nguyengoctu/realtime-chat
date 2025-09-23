package com.chatapp.gateway.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtAuthenticationFilter class.
 * Tests JWT authentication filtering logic for various scenarios.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Set up test environment before each test.
     */
    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil);
    }

    /**
     * Test that filter has correct order priority.
     * Verifies that the filter runs early in the filter chain.
     */
    @Test
    void shouldHaveHighPriorityOrder() {
        // When
        int order = jwtAuthenticationFilter.getOrder();

        // Then
        assertEquals(-1, order);
    }

    /**
     * Test filter can be created with JwtUtil dependency.
     */
    @Test
    void shouldCreateFilterWithJwtUtil() {
        // Given & When
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil);

        // Then
        assertNotNull(filter);
    }
}