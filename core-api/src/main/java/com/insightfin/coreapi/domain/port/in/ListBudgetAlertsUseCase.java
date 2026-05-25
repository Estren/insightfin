package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.BudgetAlert;

import java.util.List;
import java.util.UUID;

public interface ListBudgetAlertsUseCase {
    List<BudgetAlert> execute(UUID userId);
}
