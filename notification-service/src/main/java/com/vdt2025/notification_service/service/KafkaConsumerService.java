package com.vdt2025.notification_service.service;

import com.vdt2025.common_dto.dto.UserCreatedEvent;
import com.vdt2025.notification_service.dto.UserWelcomeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class KafkaConsumerService {
    EmailService emailService;
    @KafkaListener(topics = "welcome-email-topic", groupId = "notification-service")
    public void listen(UserCreatedEvent userCreatedEvent) {
        log.info("Received UserWelcomeMessage {}", userCreatedEvent);
        try {
            emailService.sendWelcomeEmail(userCreatedEvent.getEmail(), userCreatedEvent.getUsername());
            log.info("Sent welcome email to user: {}", userCreatedEvent.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email to user: {}", userCreatedEvent.getEmail(), e);
        }
    }
}
