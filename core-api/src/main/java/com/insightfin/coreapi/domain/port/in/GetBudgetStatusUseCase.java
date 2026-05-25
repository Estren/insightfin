package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.BudgetStatus;

import java.util.List;
import java.util.UUID;

public interface GetBudgetStatusUseCase {
    List<BudgetStatus> execute(UUID userId, String month, boolean withStatus);
}
