package com.nexushr.security;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final Resend resend;
    public EmailService(@Value("${app.resend.api-key}") String apiKey) {
        this.resend = new Resend(apiKey);
    }

    @Async
    public void sendResetEmail(String to, String resetLink) {
        String body = "Hello,\n\nYou have requested to reset your NexusHR password.\n" +
                "Click the link below to change it. This link will expire in 15 minutes.\n\n" +
                resetLink + "\n\nIf you did not request this, please ignore this email.";

        sendEmail(to, "NexusHR - Password Reset Request", body);
    }

    @Async
    public void sendGeneralEmail(String to, String subject, String body) {
        sendEmail(to, subject, body);
    }
    private void sendEmail(String to, String subject, String body) {
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("NexusHR <onboarding@resend.dev>")
                .to(to)
                .subject(subject)
                .text(body)
                .build();

        try {
            resend.emails().send(params);
            System.out.println("✅ Email successfully sent via Resend API to: " + to);
        } catch (ResendException e) {
            System.err.println("❌ Failed to send email via API: " + e.getMessage());
        }
    }
}