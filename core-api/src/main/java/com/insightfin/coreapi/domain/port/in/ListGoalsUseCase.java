package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.Goal;

import java.util.List;
import java.util.UUID;

public interface ListGoalsUseCase {
    List<Goal> execute(UUID userId);
}
