package com.insightfin.coreapi.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record AiFeedbackNotification(AiFeedback feedback) implements Notification {

    @Override
    public UUID id() {
        return feedback.getId();
    }

    @Override
    public NotificationKind kind() {
        return NotificationKind.AI_FEEDBACK;
    }

    @Override
    public boolean read() {
        return feedback.isRead();
    }

    @Override
    public LocalDateTime createdAt() {
        return feedback.getCreatedAt();
    }
}
