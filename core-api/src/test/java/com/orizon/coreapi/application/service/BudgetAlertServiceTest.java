package com.orizon.coreapi.application.service;

import com.orizon.coreapi.domain.exception.ResourceNotFoundException;
import com.orizon.coreapi.domain.model.Budget;
import com.orizon.coreapi.domain.model.BudgetAlert;
import com.orizon.coreapi.domain.port.out.BudgetAlertRepository;
import com.orizon.coreapi.domain.port.out.BudgetRepository;
import com.orizon.coreapi.domain.port.out.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetAlertServiceTest {

    @Mock BudgetAlertRepository alertRepository;
    @Mock BudgetRepository budgetRepository;
    @Mock TransactionRepository transactionRepository;

    private BudgetAlertService service;

    private UUID userId;
    private UUID categoryId;
    private UUID budgetId;
    private LocalDate txDate;

    @BeforeEach
    void setUp() {
        service = new BudgetAlertService(alertRepository, budgetRepository, transactionRepository);
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        budgetId = UUID.randomUUID();
        txDate = LocalDate.of(2026, 5, 23);
    }

    // --- B1: 50% threshold fires when spent ≥ 50% of budget ---
    @Test
    void evaluate_at50Percent_firesOnlyThe50Alert() {
        givenBudget("1000.00");
        givenSpent("500.00");
        when(alertRepository.existsForBudgetAtThreshold(eq(budgetId), anyInt())).thenReturn(false);
        when(alertRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<BudgetAlert> fired = service.execute(userId, categoryId, txDate);

        assertThat(fired).hasSize(1);
        assertThat(fired.get(0).getThresholdPercentage()).isEqualTo(50);
    }

    // --- B2: 80% fires both 50 and 80 ---
    @Test
    void evaluate_at80Percent_firesBoth50And80() {
        givenBudget("1000.00");
        givenSpent("800.00");
        when(alertRepository.existsForBudgetAtThreshold(eq(budgetId), anyInt())).thenReturn(false);
        when(alertRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<BudgetAlert> fired = service.execute(userId, categoryId, txDate);

        assertThat(fired).extracting(BudgetAlert::getThresholdPercentage).containsExactly(50, 80);
    }

    // --- B3: 100% fires all three thresholds (covers D8 — jump past intermediate levels) ---
    @Test
    void evaluate_at100Percent_firesAllThresholds() {
        givenBudget("1000.00");
        givenSpent("1000.00");
        when(alertRepository.existsForBudgetAtThreshold(eq(budgetId), anyInt())).thenReturn(false);
        when(alertRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<BudgetAlert> fired = service.execute(userId, categoryId, txDate);

        assertThat(fired).extracting(BudgetAlert::getThresholdPercentage).containsExactly(50, 80, 100);
    }

    // --- B4: under 50% fires nothing ---
    @Test
    void evaluate_at49Percent_firesNothing() {
        givenBudget("1000.00");
        givenSpent("490.00");

        List<BudgetAlert> fired = service.execute(userId, categoryId, txDate);

        assertThat(fired).isEmpty();
        verify(alertRepository, never()).save(any());
    }

    // --- B5: idempotency — already fired 50%, only fires 80% on subsequent jump ---
    @Test
    void evaluate_when50AlreadyFired_firesOnly80OnJumpTo85() {
        givenBudget("1000.00");
        givenSpent("850.00");
        when(alertRepository.existsForBudgetAtThreshold(budgetId, 50)).thenReturn(true);
        when(alertRepository.existsForBudgetAtThreshold(budgetId, 80)).thenReturn(false);
        // No stub for 100 — short-circuits because 85% < 100% never reaches that check.
        when(alertRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<BudgetAlert> fired = service.execute(userId, categoryId, txDate);

        assertThat(fired).extracting(BudgetAlert::getThresholdPercentage).containsExactly(80);
    }

    // --- B6: all thresholds already fired — no duplicates ---
    @Test
    void evaluate_whenAllThresholdsAlreadyFired_firesNothing() {
        givenBudget("1000.00");
        givenSpent("1500.00");
        when(alertRepository.existsForBudgetAtThreshold(eq(budgetId), anyInt())).thenReturn(true);

        List<BudgetAlert> fired = service.execute(userId, categoryId, txDate);

        assertThat(fired).isEmpty();
        verify(alertRepository, never()).save(any());
    }

    // --- B7: no budget for the (user, category, month) — fires nothing (also covers D7) ---
    @Test
    void evaluate_whenBudgetMissing_firesNothing() {
        when(budgetRepository.findByUserIdAndCategoryIdAndMonth(userId, categoryId, "2026-05"))
                .thenReturn(Optional.empty());

        List<BudgetAlert> fired = service.execute(userId, categoryId, txDate);

        assertThat(fired).isEmpty();
        verify(alertRepository, never()).save(any());
    }

    // --- B8: budget amount = 0 — guard against divide-by-zero, fires nothing ---
    @Test
    void evaluate_whenBudgetAmountZero_firesNothing() {
        givenBudget("0.00");

        List<BudgetAlert> fired = service.execute(userId, categoryId, txDate);

        assertThat(fired).isEmpty();
        verify(alertRepository, never()).save(any());
    }

    // --- B9: alert payload contains spent/budget snapshot at trigger time ---
    @Test
    void evaluate_capturesAmountSnapshotOnTheAlert() {
        givenBudget("200.00");
        givenSpent("110.00");
        when(alertRepository.existsForBudgetAtThreshold(eq(budgetId), anyInt())).thenReturn(false);
        ArgumentCaptor<BudgetAlert> captor = ArgumentCaptor.forClass(BudgetAlert.class);
        when(alertRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.execute(userId, categoryId, txDate);

        BudgetAlert saved = captor.getValue();
        assertThat(saved.getAmountSpent()).isEqualByComparingTo("110.00");
        assertThat(saved.getBudgetAmount()).isEqualByComparingTo("200.00");
        assertThat(saved.getThresholdPercentage()).isEqualTo(50);
        assertThat(saved.isRead()).isFalse();
    }

    // --- B10: list delegates to repo ---
    @Test
    void list_returnsAlertsFromRepository() {
        BudgetAlert alert = sampleAlert(50);
        when(alertRepository.findByUserId(userId)).thenReturn(List.of(alert));

        List<BudgetAlert> result = service.execute(userId);

        assertThat(result).containsExactly(alert);
    }

    // --- B11: markAsRead saves with read=true ---
    @Test
    void markAsRead_setsReadTrueAndSaves() {
        BudgetAlert alert = sampleAlert(80);
        UUID alertId = alert.getId();
        when(alertRepository.findById(alertId)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute(userId, alertId);

        ArgumentCaptor<BudgetAlert> captor = ArgumentCaptor.forClass(BudgetAlert.class);
        verify(alertRepository).save(captor.capture());
        assertThat(captor.getValue().isRead()).isTrue();
    }

    // --- B12: markAsRead by another user — 404 (don't leak existence) ---
    @Test
    void markAsRead_whenAlertBelongsToAnotherUser_throwsNotFound() {
        BudgetAlert alert = sampleAlert(80);
        alert.setUserId(UUID.randomUUID());
        UUID alertId = alert.getId();
        when(alertRepository.findById(alertId)).thenReturn(Optional.of(alert));

        assertThatThrownBy(() -> service.execute(userId, alertId))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(alertRepository, never()).save(any());
    }

    // --- B13: markAsRead with unknown id — 404 ---
    @Test
    void markAsRead_whenAlertMissing_throwsNotFound() {
        UUID alertId = UUID.randomUUID();
        when(alertRepository.findById(alertId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(userId, alertId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- helpers ----------

    private void givenBudget(String amount) {
        Budget budget = new Budget();
        budget.setId(budgetId);
        budget.setUserId(userId);
        budget.setCategoryId(categoryId);
        budget.setAmount(new BigDecimal(amount));
        budget.setMonth("2026-05");
        budget.setCreatedAt(LocalDateTime.now());
        when(budgetRepository.findByUserIdAndCategoryIdAndMonth(userId, categoryId, "2026-05"))
                .thenReturn(Optional.of(budget));
    }

    private void givenSpent(String spent) {
        when(transactionRepository.sumAmountByUserIdAndCategoryIdAndMonth(userId, categoryId, "2026-05"))
                .thenReturn(new BigDecimal(spent));
    }

    private BudgetAlert sampleAlert(int threshold) {
        return new BudgetAlert(UUID.randomUUID(), userId, budgetId, threshold,
                new BigDecimal("100.00"), new BigDecimal("200.00"),
                LocalDateTime.now(), false, LocalDateTime.now());
    }
}
