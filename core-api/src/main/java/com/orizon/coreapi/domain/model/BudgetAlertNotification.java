package com.orizon.coreapi.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record BudgetAlertNotification(BudgetAlert alert, String categoryName) implements Notification {

    @Override
    public UUID id() {
        return alert.getId();
    }

    @Override
    public NotificationKind kind() {
        return NotificationKind.BUDGET_ALERT;
    }

    @Override
    public boolean read() {
        return alert.isRead();
    }

    @Override
    public LocalDateTime createdAt() {
        return alert.getCreatedAt();
    }
}
