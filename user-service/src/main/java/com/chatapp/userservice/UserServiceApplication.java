package com.chatapp.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main Spring Boot application class for the User Service.
 * Handles user registration, authentication, and user management operations.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.chatapp.userservice", "com.chatapp.common"})
public class UserServiceApplication {

    /**
     * Main method to start the User Service application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}