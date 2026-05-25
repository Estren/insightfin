package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.Goal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface CreateGoalUseCase {
    Goal execute(UUID userId, String title, BigDecimal targetAmount, LocalDate deadline);
}
