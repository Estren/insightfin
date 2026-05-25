package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.Budget;

import java.util.List;
import java.util.UUID;

public interface ListBudgetsUseCase {
    List<Budget> execute(UUID userId, String month);
}
