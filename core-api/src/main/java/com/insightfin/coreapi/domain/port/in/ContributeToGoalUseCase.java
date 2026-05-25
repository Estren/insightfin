package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.GoalContribution;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface ContributeToGoalUseCase {
    GoalContribution execute(UUID goalId, BigDecimal amount, LocalDate date);
}
