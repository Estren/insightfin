package com.orizon.coreapi.adapter.out.persistence.repository;

import com.orizon.coreapi.adapter.out.persistence.entity.GoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaGoalRepository extends JpaRepository<GoalEntity, UUID> {
    List<GoalEntity> findByUserIdAndStatus(UUID userId, String status);
    List<GoalEntity> findByUserId(UUID userId);
}
