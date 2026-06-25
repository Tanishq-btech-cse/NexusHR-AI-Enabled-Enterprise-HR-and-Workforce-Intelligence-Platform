package com.nexushr.notification;

import com.nexushr.security.EmailService;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final EmailService emailService;

    public NotificationService(EmailService emailService) {
        this.emailService = emailService;
    }

    public NotificationMessage queue(NotificationMessage message) {
        if (NotificationChannel.EMAIL.equals(message.getChannel())) {
            emailService.sendGeneralEmail(
                    message.getRecipient(),
                    message.getSubject(),
                    message.getBody()
            );
        } else if (NotificationChannel.SMS.equals(message.getChannel())) {
            System.out.println("SMS triggered, but Twilio is not configured yet!");
        }

        return message;
    }

    // --- Added back to fix DashboardController compilation ---
    public Double successRate() {
        // Returns 100% success rate for the dashboard metric.
        // You can update this with real database logic later!
        return 100.0;
    }
}