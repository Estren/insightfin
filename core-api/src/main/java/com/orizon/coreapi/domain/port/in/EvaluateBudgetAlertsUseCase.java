package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.BudgetAlert;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EvaluateBudgetAlertsUseCase {
    List<BudgetAlert> execute(UUID userId, UUID categoryId, LocalDate transactionDate);
}
