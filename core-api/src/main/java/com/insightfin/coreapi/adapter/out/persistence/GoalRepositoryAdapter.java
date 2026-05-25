package com.insightfin.coreapi.adapter.out.persistence;

import com.insightfin.coreapi.adapter.out.persistence.mapper.GoalPersistenceMapper;
import com.insightfin.coreapi.adapter.out.persistence.repository.JpaGoalRepository;
import com.insightfin.coreapi.domain.model.Goal;
import com.insightfin.coreapi.domain.model.GoalStatus;
import com.insightfin.coreapi.domain.port.out.GoalRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class GoalRepositoryAdapter implements GoalRepository {

    @Inject
    JpaGoalRepository jpaGoalRepository;

    @Override
    @Transactional
    public Goal save(Goal goal) {
        var entity = GoalPersistenceMapper.toEntity(goal);
        var managed = jpaGoalRepository.getEntityManager().merge(entity);
        return GoalPersistenceMapper.toDomain(managed);
    }

    @Override
    public Optional<Goal> findById(UUID id) {
        return jpaGoalRepository.findByIdOptional(id).map(GoalPersistenceMapper::toDomain);
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

    @Override
    @Transactional
    public void deleteById(UUID id) {
        jpaGoalRepository.deleteById(id);
    }
}
