package com.insightfin.coreapi.adapter.in.web.dto;

import com.insightfin.coreapi.domain.model.AiFeedbackType;
import com.insightfin.coreapi.domain.model.NotificationKind;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Flat representation of a {@link com.insightfin.coreapi.domain.model.Notification}
 * for the frontend. Type-specific fields are null when not applicable; the
 * client reads {@code kind} to pick the right group.
 */
public record NotificationResponse(
        UUID id,
        NotificationKind kind,
        boolean read,
        LocalDateTime createdAt,
        // AI_FEEDBACK fields (null when kind=BUDGET_ALERT)
        AiFeedbackType aiFeedbackType,
        String title,
        String content,
        String referenceMonth,
        // BUDGET_ALERT fields (null when kind=AI_FEEDBACK)
        UUID budgetId,
        String categoryName,
        Integer thresholdPercentage,
        BigDecimal amountSpent,
        BigDecimal budgetAmount,
        LocalDateTime triggeredAt
) {}
