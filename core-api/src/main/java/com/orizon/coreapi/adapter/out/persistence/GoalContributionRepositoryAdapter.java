package com.orizon.coreapi.adapter.out.persistence;

import com.orizon.coreapi.adapter.out.persistence.mapper.GoalContributionPersistenceMapper;
import com.orizon.coreapi.adapter.out.persistence.repository.JpaGoalContributionRepository;
import com.orizon.coreapi.domain.model.GoalContribution;
import com.orizon.coreapi.domain.port.out.GoalContributionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class GoalContributionRepositoryAdapter implements GoalContributionRepository {

    private final JpaGoalContributionRepository jpaGoalContributionRepository;

    public GoalContributionRepositoryAdapter(JpaGoalContributionRepository jpaGoalContributionRepository) {
        this.jpaGoalContributionRepository = jpaGoalContributionRepository;
    }

    @Override
    public GoalContribution save(GoalContribution contribution) {
        var entity = GoalContributionPersistenceMapper.toEntity(contribution);
        var saved = jpaGoalContributionRepository.save(entity);
        return GoalContributionPersistenceMapper.toDomain(saved);
    }

    @Override
    public List<GoalContribution> findByGoalId(UUID goalId) {
        return jpaGoalContributionRepository.findByGoalId(goalId)
                .stream()
                .map(GoalContributionPersistenceMapper::toDomain)
                .toList();
    }
}
