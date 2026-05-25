package com.insightfin.coreapi.domain.port.in;

import java.util.UUID;

public interface DeleteBudgetUseCase {
    void execute(UUID userId, UUID budgetId);
}
