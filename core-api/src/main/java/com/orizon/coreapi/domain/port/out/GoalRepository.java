package com.orizon.coreapi.domain.port.out;

import com.orizon.coreapi.domain.model.Goal;
import com.orizon.coreapi.domain.model.GoalStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalRepository {
    Goal save(Goal goal);
    Optional<Goal> findById(UUID id);
    List<Goal> findByUserIdAndStatus(UUID userId, GoalStatus status);
    List<Goal> findByUserId(UUID userId);
}
