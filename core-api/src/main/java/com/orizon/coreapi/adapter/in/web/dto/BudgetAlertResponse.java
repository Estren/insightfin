package com.orizon.coreapi.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BudgetAlertResponse(
        UUID id,
        UUID budgetId,
        int thresholdPercentage,
        BigDecimal amountSpent,
        BigDecimal budgetAmount,
        LocalDateTime triggeredAt,
        boolean read,
        LocalDateTime createdAt
) {}
