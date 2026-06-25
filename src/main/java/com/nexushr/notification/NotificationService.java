package com.nexushr.notification;

import com.nexushr.security.EmailService;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final EmailService emailService;
    private final SmsService smsService; // <-- Add this

    // Inject both services via the constructor
    public NotificationService(EmailService emailService, SmsService smsService) {
        this.emailService = emailService;
        this.smsService = smsService;
    }

    public NotificationMessage queue(NotificationMessage message) {
        if (NotificationChannel.EMAIL.equals(message.getChannel())) {
            emailService.sendGeneralEmail(
                    message.getRecipient(),
                    message.getSubject(),
                    message.getBody()
            );
        } else if (NotificationChannel.SMS.equals(message.getChannel())) {
            // Trigger the background SMS sender!
            // Note: Twilio ignores the 'subject', so we just pass the body.
            smsService.sendSms(message.getRecipient(), message.getBody());
        }

        return message;
    }

    public Double successRate() {
        return 100.0;
    }
}