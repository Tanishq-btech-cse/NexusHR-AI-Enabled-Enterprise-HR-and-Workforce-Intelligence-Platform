package com.nexushr.notification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    NotificationMessage send(@Valid @RequestBody NotificationRequest request) {
        NotificationMessage message = new NotificationMessage();
        message.setRecipient(request.recipient());
        message.setChannel(request.channel());
        message.setSubject(request.subject());
        message.setBody(request.body());
        return service.queue(message);
    }

    public record NotificationRequest(@NotBlank String recipient, @NotNull NotificationChannel channel,
                                      @NotBlank String subject, @NotBlank String body) {
    }
}
