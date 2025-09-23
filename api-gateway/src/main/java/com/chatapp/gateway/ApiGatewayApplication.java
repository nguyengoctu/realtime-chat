package com.chatapp.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Chat Application API Gateway.
 * This service acts as the entry point for all client requests,
 * handling authentication and routing to appropriate microservices.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    /**
     * Main method to start the API Gateway application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}