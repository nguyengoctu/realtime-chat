package com.chatapp.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Spring Cloud Gateway routing.
 * Defines routes to different microservices and their corresponding paths.
 */
@Configuration
public class GatewayConfig {

        @Value("${services.user-service.url:http://user-service:8081}")
        private String userServiceUrl;

        @Value("${services.websocket-service.url:http://websocket-service:8082}")
        private String websocketServiceUrl;

        /**
         * Configures the routing rules for the API Gateway.
         * Defines which requests should be forwarded to which microservices.
         *
         * @param builder the RouteLocatorBuilder used to build routes
         * @return RouteLocator containing all configured routes
         */
        @Bean
        public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
                return builder.routes()
                                // User Service auth routes - map /api/auth to /api/users
                                .route("user-auth", r -> r
                                                .path("/api/auth/register")
                                                .filters(f -> f.rewritePath("/api/auth/register",
                                                                "/api/users/register"))
                                                .uri(userServiceUrl))
                                .route("user-login", r -> r
                                                .path("/api/auth/login")
                                                .filters(f -> f.rewritePath("/api/auth/login", "/api/users/login"))
                                                .uri(userServiceUrl))
                                .route("user-logout", r -> r
                                                .path("/api/auth/logout")
                                                .filters(f -> f.rewritePath("/api/auth/logout", "/api/users/logout"))
                                                .uri(userServiceUrl))
                                .route("user-refresh", r -> r
                                                .path("/api/auth/refresh")
                                                .filters(f -> f.rewritePath("/api/auth/refresh", "/api/users/refresh"))
                                                .uri(userServiceUrl))

                                // User Service routes
                                .route("user-service", r -> r
                                                .path("/api/users/**")
                                                .uri(userServiceUrl))

                                // WebSocket Service routes (for future implementation)
                                .route("websocket-service", r -> r
                                                .path("/api/chat/**", "/ws/**")
                                                .uri(websocketServiceUrl))

                                // Health check routes
                                .route("health-check", r -> r
                                                .path("/health", "/actuator/**")
                                                .uri(userServiceUrl))

                                .build();
        }
}