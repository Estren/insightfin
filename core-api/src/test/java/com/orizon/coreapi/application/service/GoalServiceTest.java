package com.orizon.coreapi.application.service;

import com.orizon.coreapi.domain.event.GoalContributedEvent;
import com.orizon.coreapi.domain.exception.ResourceNotFoundException;
import com.orizon.coreapi.domain.model.Goal;
import com.orizon.coreapi.domain.model.GoalContribution;
import com.orizon.coreapi.domain.model.GoalStatus;
import com.orizon.coreapi.domain.port.out.EventPublisher;
import com.orizon.coreapi.domain.port.out.GoalContributionRepository;
import com.orizon.coreapi.domain.port.out.GoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock GoalRepository goalRepository;
    @Mock GoalContributionRepository goalContributionRepository;
    @Mock EventPublisher eventPublisher;

    private GoalService service;

    @BeforeEach
    void setUp() {
        service = new GoalService(goalRepository, goalContributionRepository, eventPublisher);
    }

    // --- G1 ---
    @Test
    void create_initializes_withActiveStatusAndZeroCurrentAmount() {
        UUID userId = UUID.randomUUID();
        LocalDate deadline = LocalDate.of(2026, 12, 31);

        Goal saved = buildGoal(UUID.randomUUID(), userId, new BigDecimal("1000.00"), BigDecimal.ZERO, GoalStatus.ACTIVE);
        when(goalRepository.save(any())).thenReturn(saved);

        Goal result = service.execute(userId, "Emergency Fund", new BigDecimal("1000.00"), deadline);

        assertThat(result.getStatus()).isEqualTo(GoalStatus.ACTIVE);
        assertThat(result.getCurrentAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(goalRepository).save(any(Goal.class));
    }

    // --- G2 ---
    @Test
    void contribute_succeeds_updatesCurrentAmountAndSavesGoal() {
        UUID goalId = UUID.randomUUID();
        Goal goal = buildGoal(goalId, UUID.randomUUID(), new BigDecimal("1000.00"), new BigDecimal("200.00"), GoalStatus.ACTIVE);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));

        GoalContribution savedContribution = buildContribution(UUID.randomUUID(), goalId, new BigDecimal("300.00"));
        when(goalContributionRepository.save(any())).thenReturn(savedContribution);

        service.execute(goalId, new BigDecimal("300.00"), LocalDate.now());

        assertThat(goal.getCurrentAmount()).isEqualByComparingTo("500.00");
        verify(goalRepository).save(goal);
    }

    // --- G3 ---
    @Test
    void contribute_completesGoal_whenTargetReached() {
        UUID goalId = UUID.randomUUID();
        Goal goal = buildGoal(goalId, UUID.randomUUID(), new BigDecimal("1000.00"), new BigDecimal("700.00"), GoalStatus.ACTIVE);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(goalContributionRepository.save(any())).thenReturn(buildContribution(UUID.randomUUID(), goalId, new BigDecimal("300.00")));

        service.execute(goalId, new BigDecimal("300.00"), LocalDate.now());

        assertThat(goal.getStatus()).isEqualTo(GoalStatus.COMPLETED);
        assertThat(goal.getCurrentAmount()).isEqualByComparingTo("1000.00");
    }

    // --- G4 ---
    @Test
    void contribute_publishesGoalContributedEvent() {
        UUID goalId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Goal goal = buildGoal(goalId, userId, new BigDecimal("1000.00"), new BigDecimal("400.00"), GoalStatus.ACTIVE);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(goalContributionRepository.save(any())).thenReturn(buildContribution(UUID.randomUUID(), goalId, new BigDecimal("100.00")));

        service.execute(goalId, new BigDecimal("100.00"), LocalDate.now());

        ArgumentCaptor<GoalContributedEvent> captor = ArgumentCaptor.forClass(GoalContributedEvent.class);
        verify(eventPublisher).publishGoalContributed(captor.capture());

        GoalContributedEvent event = captor.getValue();
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.goalId()).isEqualTo(goalId);
        assertThat(event.amount()).isEqualByComparingTo("100.00");
        assertThat(event.currentAmount()).isEqualByComparingTo("500.00");
        assertThat(event.targetAmount()).isEqualByComparingTo("1000.00");
    }

    // --- G5 ---
    @Test
    void contribute_throwsWhenGoalNotFound() {
        UUID goalId = UUID.randomUUID();
        when(goalRepository.findById(goalId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(goalId, new BigDecimal("100.00"), LocalDate.now()))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(goalContributionRepository, eventPublisher);
    }

    // --- G6 ---
    @Test
    void update_throwsWhenOwnershipMismatch() {
        UUID goalId = UUID.randomUUID();
        UUID realOwner = UUID.randomUUID();
        UUID attacker = UUID.randomUUID();

        Goal goal = buildGoal(goalId, realOwner, new BigDecimal("500.00"), BigDecimal.ZERO, GoalStatus.ACTIVE);
        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));

        assertThatThrownBy(() ->
                service.execute(attacker, goalId, "New Title", new BigDecimal("600.00"), LocalDate.now()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(goalRepository, never()).save(any());
    }

    // --- G7 ---
    @Test
    void delete_succeeds_callsDeleteById() {
        UUID userId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();

        Goal goal = buildGoal(goalId, userId, new BigDecimal("500.00"), BigDecimal.ZERO, GoalStatus.ACTIVE);
        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));

        service.execute(userId, goalId);

        verify(goalRepository).deleteById(goalId);
    }

    // --- G8 ---
    @Test
    void delete_throwsWhenOwnershipMismatch() {
        UUID goalId = UUID.randomUUID();
        UUID realOwner = UUID.randomUUID();
        UUID attacker = UUID.randomUUID();

        Goal goal = buildGoal(goalId, realOwner, new BigDecimal("500.00"), BigDecimal.ZERO, GoalStatus.ACTIVE);
        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));

        assertThatThrownBy(() -> service.execute(attacker, goalId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(goalRepository, never()).deleteById(any());
    }

    // --- fixtures ---

    private Goal buildGoal(UUID id, UUID userId, BigDecimal target, BigDecimal current, GoalStatus status) {
        Goal g = new Goal();
        g.setId(id);
        g.setUserId(userId);
        g.setTitle("Test Goal");
        g.setTargetAmount(target);
        g.setCurrentAmount(current);
        g.setDeadline(LocalDate.of(2026, 12, 31));
        g.setStatus(status);
        g.setCreatedAt(LocalDateTime.now());
        g.setUpdatedAt(LocalDateTime.now());
        return g;
    }

    private GoalContribution buildContribution(UUID id, UUID goalId, BigDecimal amount) {
        GoalContribution c = new GoalContribution();
        c.setId(id);
        c.setGoalId(goalId);
        c.setAmount(amount);
        c.setDate(LocalDate.now());
        c.setCreatedAt(LocalDateTime.now());
        return c;
    }
}
