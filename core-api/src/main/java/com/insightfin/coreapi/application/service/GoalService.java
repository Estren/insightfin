package com.insightfin.coreapi.application.service;

import com.insightfin.coreapi.domain.event.GoalContributedEvent;
import com.insightfin.coreapi.domain.exception.ResourceNotFoundException;
import com.insightfin.coreapi.domain.model.Goal;
import com.insightfin.coreapi.domain.model.GoalContribution;
import com.insightfin.coreapi.domain.model.GoalStatus;
import com.insightfin.coreapi.domain.port.in.ContributeToGoalUseCase;
import com.insightfin.coreapi.domain.port.in.CreateGoalUseCase;
import com.insightfin.coreapi.domain.port.in.DeleteGoalUseCase;
import com.insightfin.coreapi.domain.port.in.ListGoalsUseCase;
import com.insightfin.coreapi.domain.port.in.UpdateGoalUseCase;
import com.insightfin.coreapi.domain.port.out.EventPublisher;
import com.insightfin.coreapi.domain.port.out.GoalContributionRepository;
import com.insightfin.coreapi.domain.port.out.GoalRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class GoalService implements CreateGoalUseCase, ContributeToGoalUseCase,
        UpdateGoalUseCase, DeleteGoalUseCase, ListGoalsUseCase {

    private final GoalRepository goalRepository;
    private final GoalContributionRepository goalContributionRepository;
    private final EventPublisher eventPublisher;

    public GoalService(GoalRepository goalRepository,
                       GoalContributionRepository goalContributionRepository,
                       EventPublisher eventPublisher) {
        this.goalRepository = goalRepository;
        this.goalContributionRepository = goalContributionRepository;
        this.eventPublisher = eventPublisher;
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

        eventPublisher.publishGoalContributed(new GoalContributedEvent(
                goal.getUserId(), goalId, amount,
                goal.getCurrentAmount(), goal.getTargetAmount()));

        return saved;
    }

    @Override
    public Goal execute(UUID userId, UUID goalId, String title, BigDecimal targetAmount, LocalDate deadline) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", goalId));

        if (!goal.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Goal", goalId);
        }

        goal.setTitle(title);
        goal.setTargetAmount(targetAmount);
        goal.setDeadline(deadline);
        goal.setUpdatedAt(LocalDateTime.now());

        return goalRepository.save(goal);
    }

    @Override
    public void execute(UUID userId, UUID goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", goalId));

        if (!goal.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Goal", goalId);
        }

        goalRepository.deleteById(goalId);
    }

    @Override
    public List<Goal> execute(UUID userId) {
        return goalRepository.findByUserId(userId);
    }
}
