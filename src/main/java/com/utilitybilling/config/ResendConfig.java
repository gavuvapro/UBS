package com.utilitybilling.config;

import com.resend.Resend;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Resend email SDK configuration.
 * Creates a Resend client bean using the API key from environment/application.yml.
 */
@Configuration
public class ResendConfig {

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Bean
    public Resend resendClient() {
        return new Resend(resendApiKey);
    }
}
