package com.orizon.coreapi.application.service;

import com.orizon.coreapi.domain.exception.ResourceNotFoundException;
import com.orizon.coreapi.domain.model.Goal;
import com.orizon.coreapi.domain.model.GoalContribution;
import com.orizon.coreapi.domain.model.GoalStatus;
import com.orizon.coreapi.domain.port.in.ContributeToGoalUseCase;
import com.orizon.coreapi.domain.port.in.CreateGoalUseCase;
import com.orizon.coreapi.domain.port.out.GoalContributionRepository;
import com.orizon.coreapi.domain.port.out.GoalRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class GoalService implements CreateGoalUseCase, ContributeToGoalUseCase {

    private final GoalRepository goalRepository;
    private final GoalContributionRepository goalContributionRepository;

    public GoalService(GoalRepository goalRepository,
                       GoalContributionRepository goalContributionRepository) {
        this.goalRepository = goalRepository;
        this.goalContributionRepository = goalContributionRepository;
    }

    @Override
    public Goal execute(UUID userId, String title, BigDecimal targetAmount, LocalDate deadline) {
        Goal goal = new Goal();
        goal.setId(UUID.randomUUID());
        goal.setUserId(userId);
        goal.setTitle(title);
        goal.setTargetAmount(targetAmount);
        goal.setCurrentAmount(BigDecimal.ZERO);
        goal.setDeadline(deadline);
        goal.setStatus(GoalStatus.ACTIVE);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());

        return goalRepository.save(goal);
    }

    @Override
    public GoalContribution execute(UUID goalId, BigDecimal amount, LocalDate date) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", goalId));

        GoalContribution contribution = new GoalContribution();
        contribution.setId(UUID.randomUUID());
        contribution.setGoalId(goalId);
        contribution.setAmount(amount);
        contribution.setDate(date);
        contribution.setCreatedAt(LocalDateTime.now());

        GoalContribution saved = goalContributionRepository.save(contribution);

        goal.setCurrentAmount(goal.getCurrentAmount().add(amount));
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED);
        }
        goal.setUpdatedAt(LocalDateTime.now());
        goalRepository.save(goal);

        return saved;
    }
}
