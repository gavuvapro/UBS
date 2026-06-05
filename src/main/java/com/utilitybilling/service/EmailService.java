package com.utilitybilling.service;

/**
 * Interface for email delivery abstraction.
 * Implemented by ResendEmailService for production email sending.
 */
public interface EmailService {

    /**
     * Sends an HTML email to the specified recipient.
     *
     * @param to        recipient email address
     * @param subject   email subject line
     * @param htmlBody  HTML content of the email
     */
    void sendEmail(String to, String subject, String htmlBody);
}
