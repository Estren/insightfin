package com.orizon.coreapi.domain.port.in;

import java.math.BigDecimal;
import java.util.UUID;

import com.orizon.coreapi.domain.model.Budget;

public interface UpdateBudgetUseCase {
    Budget execute(UUID userId, UUID budgetId, BigDecimal amount);
}
