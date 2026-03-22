package com.orizon.coreapi.adapter.out.persistence;

import com.orizon.coreapi.adapter.out.persistence.mapper.GoalPersistenceMapper;
import com.orizon.coreapi.adapter.out.persistence.repository.JpaGoalRepository;
import com.orizon.coreapi.domain.model.Goal;
import com.orizon.coreapi.domain.model.GoalStatus;
import com.orizon.coreapi.domain.port.out.GoalRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class GoalRepositoryAdapter implements GoalRepository {

    private final JpaGoalRepository jpaGoalRepository;

    public GoalRepositoryAdapter(JpaGoalRepository jpaGoalRepository) {
        this.jpaGoalRepository = jpaGoalRepository;
    }

    @Override
    public Goal save(Goal goal) {
        var entity = GoalPersistenceMapper.toEntity(goal);
        var saved = jpaGoalRepository.save(entity);
        return GoalPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Goal> findById(UUID id) {
        return jpaGoalRepository.findById(id).map(GoalPersistenceMapper::toDomain);
    }

    @Override
    public List<Goal> findByUserIdAndStatus(UUID userId, GoalStatus status) {
        return jpaGoalRepository.findByUserIdAndStatus(userId, status.name())
                .stream()
                .map(GoalPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Goal> findByUserId(UUID userId) {
        return jpaGoalRepository.findByUserId(userId)
                .stream()
                .map(GoalPersistenceMapper::toDomain)
                .toList();
    }
}
