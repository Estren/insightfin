package com.insightfin.coreapi.adapter.out.persistence.repository;

import com.insightfin.coreapi.adapter.out.persistence.entity.GoalEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class JpaGoalRepository implements PanacheRepositoryBase<GoalEntity, UUID> {

    public List<GoalEntity> findByUserIdAndStatus(UUID userId, String status) {
        return list("userId = ?1 and status = ?2", userId, status);
    }

    public List<GoalEntity> findByUserId(UUID userId) {
        return list("userId", userId);
    }
}
