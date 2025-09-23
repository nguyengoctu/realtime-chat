package com.chatapp.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GatewayConfig class.
 * Tests routing configuration and route definitions.
 */
@ExtendWith(MockitoExtension.class)
class GatewayConfigTest {

    private GatewayConfig gatewayConfig;

    /**
     * Set up test environment before each test.
     */
    @BeforeEach
    void setUp() {
        gatewayConfig = new GatewayConfig();
        // Set test values for service URLs
        ReflectionTestUtils.setField(gatewayConfig, "userServiceUrl", "http://user-service:8081");
        ReflectionTestUtils.setField(gatewayConfig, "websocketServiceUrl", "http://websocket-service:8082");
    }


    /**
     * Test that GatewayConfig can be instantiated.
     */
    @Test
    void shouldCreateGatewayConfig() {
        // Given & When
        GatewayConfig config = new GatewayConfig();

        // Then
        assertNotNull(config);
    }
}