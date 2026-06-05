package com.utilitybilling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main Spring Boot application entry point for the Utility Billing System.
 * Enables JPA auditing for automatic createdAt/updatedAt population.
 */
@SpringBootApplication
@EnableJpaAuditing
public class UtilityBillingApplication {

    public static void main(String[] args) {
        SpringApplication.run(UtilityBillingApplication.class, args);
    }
}
