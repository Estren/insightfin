package com.orizon.coreapi.domain.port.out;

import com.orizon.coreapi.domain.model.BudgetAlert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetAlertRepository {
    BudgetAlert save(BudgetAlert alert);
    Optional<BudgetAlert> findById(UUID id);
    List<BudgetAlert> findByUserId(UUID userId);
    boolean existsForBudgetAtThreshold(UUID budgetId, int thresholdPercentage);
    int countUnreadByUserId(UUID userId);
}
