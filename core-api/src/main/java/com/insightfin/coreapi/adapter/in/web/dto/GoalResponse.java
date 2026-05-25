package com.insightfin.coreapi.adapter.in.web.dto;

import com.insightfin.coreapi.domain.model.GoalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record GoalResponse(
        UUID id,
        String title,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        LocalDate deadline,
        GoalStatus status,
        LocalDateTime createdAt
) {}
