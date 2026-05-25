package com.insightfin.coreapi.adapter.out.persistence.repository;

import com.insightfin.coreapi.adapter.out.persistence.entity.GoalContributionEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class JpaGoalContributionRepository implements PanacheRepositoryBase<GoalContributionEntity, UUID> {

    public List<GoalContributionEntity> findByGoalId(UUID goalId) {
        return list("goalId", goalId);
    }
}
