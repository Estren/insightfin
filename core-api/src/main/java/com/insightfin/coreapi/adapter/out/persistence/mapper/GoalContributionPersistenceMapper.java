package com.insightfin.coreapi.adapter.out.persistence.mapper;

import com.insightfin.coreapi.adapter.out.persistence.entity.GoalContributionEntity;
import com.insightfin.coreapi.domain.model.GoalContribution;

public class GoalContributionPersistenceMapper {

    private GoalContributionPersistenceMapper() {}

    public static GoalContributionEntity toEntity(GoalContribution contribution) {
        GoalContributionEntity entity = new GoalContributionEntity();
        entity.setId(contribution.getId());
        entity.setGoalId(contribution.getGoalId());
        entity.setAmount(contribution.getAmount());
        entity.setDate(contribution.getDate());
        entity.setCreatedAt(contribution.getCreatedAt());
        return entity;
    }

    public static GoalContribution toDomain(GoalContributionEntity entity) {
        return new GoalContribution(
                entity.getId(),
                entity.getGoalId(),
                entity.getAmount(),
                entity.getDate(),
                entity.getCreatedAt()
        );
    }
}
