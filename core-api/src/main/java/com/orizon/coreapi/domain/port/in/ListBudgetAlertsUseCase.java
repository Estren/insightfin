package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.BudgetAlert;

import java.util.List;
import java.util.UUID;

public interface ListBudgetAlertsUseCase {
    List<BudgetAlert> execute(UUID userId);
}
