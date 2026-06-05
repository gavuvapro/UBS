package com.utilitybilling.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Resend SDK implementation of EmailService.
 * Uses the com.resend.Resend client to send transactional HTML emails.
 * All failures are logged gracefully; the system continues operation if email delivery fails.
 */
@Service
public class ResendEmailService implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(ResendEmailService.class);

    private final Resend resend;

    @Value("${resend.from}")
    private String fromAddress;

    public ResendEmailService(Resend resend) {
        this.resend = resend;
    }

    @Override
    public void sendEmail(String to, String subject, String htmlBody) {
        try {
            CreateEmailOptions request = CreateEmailOptions.builder()
                    .from(fromAddress)
                    .to(to)
                    .subject(subject)
                    .html(htmlBody)
                    .build();

            resend.emails().send(request);
            logger.info("Email sent successfully to {}", to);
        } catch (ResendException e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error sending email to {}: {}", to, e.getMessage());
        }
    }
}