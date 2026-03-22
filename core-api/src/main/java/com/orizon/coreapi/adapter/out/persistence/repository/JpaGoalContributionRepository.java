package com.orizon.coreapi.adapter.out.persistence.repository;

import com.orizon.coreapi.adapter.out.persistence.entity.GoalContributionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaGoalContributionRepository extends JpaRepository<GoalContributionEntity, UUID> {
    List<GoalContributionEntity> findByGoalId(UUID goalId);
}
