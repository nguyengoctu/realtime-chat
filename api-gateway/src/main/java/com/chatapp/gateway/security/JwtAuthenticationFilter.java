package com.chatapp.gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Global filter for JWT authentication in the API Gateway.
 * This filter intercepts all incoming requests and validates JWT tokens
 * before forwarding requests to downstream services.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    private static final String BEARER_PREFIX = "Bearer ";
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/logout",
            "/health",
            "/actuator"
    );

    /**
     * Filters incoming requests to validate JWT tokens.
     *
     * @param exchange the current server exchange
     * @param chain    provides a way to delegate to the next filter
     * @return a Mono representing the completion of the filter chain
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Skip authentication for public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return handleUnauthorized(exchange);
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        if (!jwtUtil.validateToken(token)) {
            return handleUnauthorized(exchange);
        }

        try {
            String username = jwtUtil.getUsernameFromToken(token);

            // Add username to headers for downstream services
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", username)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            return handleUnauthorized(exchange);
        }
    }

    /**
     * Checks if the requested path is a public endpoint that doesn't require authentication.
     *
     * @param path the request path
     * @return true if the path is public, false otherwise
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Handles unauthorized requests by setting appropriate HTTP status and response.
     *
     * @param exchange the current server exchange
     * @return a Mono representing the unauthorized response
     */
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");

        String body = "{\"error\":\"Unauthorized\",\"message\":\"Invalid or missing JWT token\"}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    /**
     * Defines the order of this filter in the filter chain.
     *
     * @return the order value (lower values have higher priority)
     */
    @Override
    public int getOrder() {
        return -1; // High priority to run before other filters
    }
}