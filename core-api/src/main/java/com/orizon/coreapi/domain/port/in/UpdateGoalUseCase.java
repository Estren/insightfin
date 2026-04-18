package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.Goal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface UpdateGoalUseCase {
    Goal execute(UUID userId, UUID goalId, String title, BigDecimal targetAmount, LocalDate deadline);
}
