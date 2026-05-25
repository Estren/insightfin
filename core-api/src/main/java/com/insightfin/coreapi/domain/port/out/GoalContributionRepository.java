package com.insightfin.coreapi.domain.port.out;

import com.insightfin.coreapi.domain.model.GoalContribution;

import java.util.List;
import java.util.UUID;

public interface GoalContributionRepository {
    GoalContribution save(GoalContribution contribution);
    List<GoalContribution> findByGoalId(UUID goalId);
}
