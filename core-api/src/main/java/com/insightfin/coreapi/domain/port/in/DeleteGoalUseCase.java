package com.insightfin.coreapi.domain.port.in;

import java.util.UUID;

public interface DeleteGoalUseCase {
    void execute(UUID userId, UUID goalId);
}
