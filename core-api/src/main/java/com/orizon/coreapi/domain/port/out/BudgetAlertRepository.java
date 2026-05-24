package com.orizon.coreapi.domain.port.out;

import com.orizon.coreapi.domain.model.BudgetAlert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetAlertRepository {
    BudgetAlert save(BudgetAlert alert);
    Optional<BudgetAlert> findById(UUID id);
    List<BudgetAlert> findByUserId(UUID userId);
    boolean existsForBudgetAtThreshold(UUID budgetId, int thresholdPercentage);
    int countUnreadByUserId(UUID userId);

    /**
     * Keyset pagination ordered by {@code (createdAt DESC, id DESC)}.
     *
     * <p>If {@code cursorCreatedAt} is {@code null}, returns the first {@code limit}
     * rows from the top. Otherwise returns the next {@code limit} rows strictly
     * after the {@code (cursorCreatedAt, cursorId)} position.
     */
    List<BudgetAlert> findPage(UUID userId, LocalDateTime cursorCreatedAt, UUID cursorId, int limit);
}
