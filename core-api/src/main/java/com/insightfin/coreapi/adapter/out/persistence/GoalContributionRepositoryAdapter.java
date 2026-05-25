package com.insightfin.coreapi.adapter.out.persistence;

import com.insightfin.coreapi.adapter.out.persistence.mapper.GoalContributionPersistenceMapper;
import com.insightfin.coreapi.adapter.out.persistence.repository.JpaGoalContributionRepository;
import com.insightfin.coreapi.domain.model.GoalContribution;
import com.insightfin.coreapi.domain.port.out.GoalContributionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class GoalContributionRepositoryAdapter implements GoalContributionRepository {

    @Inject
    JpaGoalContributionRepository jpaGoalContributionRepository;

    @Override
    @Transactional
    public GoalContribution save(GoalContribution contribution) {
        var entity = GoalContributionPersistenceMapper.toEntity(contribution);
        jpaGoalContributionRepository.persist(entity);
        return GoalContributionPersistenceMapper.toDomain(entity);
    }

    @Override
    public List<GoalContribution> findByGoalId(UUID goalId) {
        return jpaGoalContributionRepository.findByGoalId(goalId)
                .stream()
                .map(GoalContributionPersistenceMapper::toDomain)
                .toList();
    }
}
