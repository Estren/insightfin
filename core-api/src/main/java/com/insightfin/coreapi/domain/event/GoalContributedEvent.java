package com.insightfin.coreapi.domain.event;

import java.math.BigDecimal;
import java.util.UUID;

public record GoalContributedEvent(
        UUID userId,
        UUID goalId,
        BigDecimal amount,
        BigDecimal currentAmount,
        BigDecimal targetAmount) {
}
