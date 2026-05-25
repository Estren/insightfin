package com.insightfin.coreapi.domain.port.in;

import java.math.BigDecimal;
import java.util.UUID;

import com.insightfin.coreapi.domain.model.Budget;

public interface UpdateBudgetUseCase {
    Budget execute(UUID userId, UUID budgetId, BigDecimal amount);
}
