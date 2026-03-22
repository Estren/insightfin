package com.orizon.coreapi.adapter.out.persistence.mapper;

import com.orizon.coreapi.adapter.out.persistence.entity.GoalEntity;
import com.orizon.coreapi.domain.model.Goal;
import com.orizon.coreapi.domain.model.GoalStatus;

public class GoalPersistenceMapper {

    private GoalPersistenceMapper() {}

    public static GoalEntity toEntity(Goal goal) {
        GoalEntity entity = new GoalEntity();
        entity.setId(goal.getId());
        entity.setUserId(goal.getUserId());
        entity.setTitle(goal.getTitle());
        entity.setTargetAmount(goal.getTargetAmount());
        entity.setCurrentAmount(goal.getCurrentAmount());
        entity.setDeadline(goal.getDeadline());
        entity.setStatus(goal.getStatus().name());
        entity.setCreatedAt(goal.getCreatedAt());
        entity.setUpdatedAt(goal.getUpdatedAt());
        return entity;
    }

    public static Goal toDomain(GoalEntity entity) {
        return new Goal(
                entity.getId(),
                entity.getUserId(),
                entity.getTitle(),
                entity.getTargetAmount(),
                entity.getCurrentAmount(),
                entity.getDeadline(),
                GoalStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
