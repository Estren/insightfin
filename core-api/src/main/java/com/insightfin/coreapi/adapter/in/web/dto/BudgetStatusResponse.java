package com.insightfin.coreapi.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetStatusResponse(
        UUID budgetId,
        UUID categoryId,
        String categoryName,
        BigDecimal budgetAmount,
        BigDecimal spentAmount,
        BigDecimal percentageUsed
) {}
