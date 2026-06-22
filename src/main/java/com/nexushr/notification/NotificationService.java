package com.nexushr.notification;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class NotificationService {
    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    @Async
    public void sendAsync(UUID id) {
        NotificationMessage message = repository.findById(id).orElseThrow();
        try {
            message.setStatus(NotificationStatus.SENT);
            message.setDeliveredAt(Instant.now());
        } catch (RuntimeException ex) {
            message.setStatus(NotificationStatus.FAILED);
            message.setFailureReason(ex.getMessage());
        }
        repository.save(message);
    }

    public NotificationMessage queue(NotificationMessage message) {
        NotificationMessage saved = repository.save(message);
        sendAsync(saved.getId());
        return saved;
    }

    public double successRate() {
        long sent = repository.countByStatus(NotificationStatus.SENT);
        long failed = repository.countByStatus(NotificationStatus.FAILED);
        long total = sent + failed;
        return total == 0 ? 1.0 : (double) sent / total;
    }
}
