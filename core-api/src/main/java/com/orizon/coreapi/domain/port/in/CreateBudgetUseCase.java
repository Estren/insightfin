package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.Budget;

import java.math.BigDecimal;
import java.util.UUID;

public interface CreateBudgetUseCase {
    Budget execute(UUID userId, UUID categoryId, BigDecimal amount, String month);
}
