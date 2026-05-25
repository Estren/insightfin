package com.insightfin.coreapi.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record GoalContributionResponse(
        UUID id,
        UUID goalId,
        BigDecimal amount,
        LocalDate date,
        LocalDateTime createdAt
) {}
