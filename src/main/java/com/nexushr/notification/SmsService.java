package com.nexushr.notification;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @Value("${app.twilio.account-sid}")
    private String accountSid;

    @Value("${app.twilio.auth-token}")
    private String authToken;

    @Value("${app.twilio.phone-number}")
    private String fromPhoneNumber;
    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.isEmpty()) {
            Twilio.init(accountSid, authToken);
        }
    }

    @Async
    public void sendSms(String toPhoneNumber, String body) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    body
            ).create();

            System.out.println("✅ SMS successfully sent via Twilio! Message SID: " + message.getSid());
        } catch (Exception e) {
            System.err.println("❌ Failed to send SMS via Twilio: " + e.getMessage());
        }
    }
}