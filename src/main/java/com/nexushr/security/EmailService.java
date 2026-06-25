package com.nexushr.security;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async // Runs in the background so it doesn't freeze the frontend while sending
    public void sendResetEmail(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("NexusHR - Password Reset Request");
        message.setText("Hello,\n\n" +
                "You have requested to reset your NexusHR password.\n" +
                "Click the link below to change it. This link will expire in 15 minutes.\n\n" +
                resetLink + "\n\n" +
                "If you did not request this, please ignore this email.");

        mailSender.send(message);
    }

    @Async
    public void sendGeneralEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}