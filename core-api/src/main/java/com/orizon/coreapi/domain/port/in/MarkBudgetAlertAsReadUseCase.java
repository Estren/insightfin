package com.orizon.coreapi.domain.port.in;

import java.util.UUID;

public interface MarkBudgetAlertAsReadUseCase {
    void execute(UUID userId, UUID alertId);
}
