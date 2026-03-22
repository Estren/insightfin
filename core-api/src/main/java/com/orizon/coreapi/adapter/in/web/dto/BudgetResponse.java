package com.orizon.coreapi.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BudgetResponse(
        UUID id,
        UUID categoryId,
        BigDecimal amount,
        String month,
        LocalDateTime createdAt
) {}
