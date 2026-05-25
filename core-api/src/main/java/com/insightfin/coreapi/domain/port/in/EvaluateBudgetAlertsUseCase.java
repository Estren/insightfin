package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.BudgetAlert;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EvaluateBudgetAlertsUseCase {
    List<BudgetAlert> execute(UUID userId, UUID categoryId, LocalDate transactionDate);
}
