package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.Goal;

import java.util.List;
import java.util.UUID;

public interface ListGoalsUseCase {
    List<Goal> execute(UUID userId);
}
