package com.insightfin.coreapi.application.service;

import com.insightfin.coreapi.domain.model.*;
import com.insightfin.coreapi.domain.port.in.GetBudgetStatusUseCase;
import com.insightfin.coreapi.domain.port.out.GoalRepository;
import com.insightfin.coreapi.domain.port.out.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock TransactionRepository transactionRepository;
    @Mock GoalRepository goalRepository;
    @Mock GetBudgetStatusUseCase getBudgetStatusUseCase;

    private DashboardService service;

    @BeforeEach
    void setUp() {
        service = new DashboardService(transactionRepository, goalRepository, getBudgetStatusUseCase);
    }

    // --- D1 ---
    @Test
    void getSummary_emptyMonth_returnsZeros() {
        UUID userId = UUID.randomUUID();

        when(transactionRepository.findByUserIdAndDateBetween(eq(userId), any(), any()))
                .thenReturn(List.of());
        when(goalRepository.findByUserIdAndStatus(userId, GoalStatus.ACTIVE))
                .thenReturn(List.of());
        when(getBudgetStatusUseCase.execute(userId, "2026-05", true))
                .thenReturn(List.of());

        DashboardSummary result = service.execute(userId, "2026-05");

        assertThat(result.getTotalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getRecentTransactions()).isEmpty();
    }

    // --- D2 ---
    @Test
    void getSummary_withTransactions_calculatesCorrectTotals() {
        UUID userId = UUID.randomUUID();

        List<Transaction> transactions = List.of(
                buildTransaction(userId, TransactionType.INCOME, "5000.00", LocalDate.of(2026, 5, 10)),
                buildTransaction(userId, TransactionType.EXPENSE, "2000.00", LocalDate.of(2026, 5, 15)),
                buildTransaction(userId, TransactionType.EXPENSE, "1000.00", LocalDate.of(2026, 5, 20))
        );

        when(transactionRepository.findByUserIdAndDateBetween(eq(userId), any(), any()))
                .thenReturn(transactions);
        when(goalRepository.findByUserIdAndStatus(userId, GoalStatus.ACTIVE)).thenReturn(List.of());
        when(getBudgetStatusUseCase.execute(userId, "2026-05", true)).thenReturn(List.of());

        DashboardSummary result = service.execute(userId, "2026-05");

        assertThat(result.getTotalIncome()).isEqualByComparingTo("5000.00");
        assertThat(result.getTotalExpense()).isEqualByComparingTo("3000.00");
        assertThat(result.getBalance()).isEqualByComparingTo("2000.00");
    }

    // --- D3 ---
    @Test
    void getSummary_recentTransactions_limitsToFiveAndOrdersByDateDesc() {
        UUID userId = UUID.randomUUID();

        List<Transaction> transactions = List.of(
                buildTransaction(userId, TransactionType.EXPENSE, "10.00", LocalDate.of(2026, 5, 1)),
                buildTransaction(userId, TransactionType.EXPENSE, "20.00", LocalDate.of(2026, 5, 7)),
                buildTransaction(userId, TransactionType.EXPENSE, "30.00", LocalDate.of(2026, 5, 14)),
                buildTransaction(userId, TransactionType.EXPENSE, "40.00", LocalDate.of(2026, 5, 21)),
                buildTransaction(userId, TransactionType.EXPENSE, "50.00", LocalDate.of(2026, 5, 25)),
                buildTransaction(userId, TransactionType.EXPENSE, "60.00", LocalDate.of(2026, 5, 28)),
                buildTransaction(userId, TransactionType.EXPENSE, "70.00", LocalDate.of(2026, 5, 30))
        );

        when(transactionRepository.findByUserIdAndDateBetween(eq(userId), any(), any()))
                .thenReturn(transactions);
        when(goalRepository.findByUserIdAndStatus(userId, GoalStatus.ACTIVE)).thenReturn(List.of());
        when(getBudgetStatusUseCase.execute(userId, "2026-05", true)).thenReturn(List.of());

        DashboardSummary result = service.execute(userId, "2026-05");

        assertThat(result.getRecentTransactions()).hasSize(5);
        assertThat(result.getRecentTransactions().get(0).getDate())
                .isEqualTo(LocalDate.of(2026, 5, 30));
        assertThat(result.getRecentTransactions().get(4).getDate())
                .isEqualTo(LocalDate.of(2026, 5, 14));
    }

    // --- D4 ---
    @Test
    void getSummary_onlyFetchesActiveGoals() {
        UUID userId = UUID.randomUUID();

        Goal activeGoal = buildGoal(userId, GoalStatus.ACTIVE);

        when(transactionRepository.findByUserIdAndDateBetween(eq(userId), any(), any()))
                .thenReturn(List.of());
        when(goalRepository.findByUserIdAndStatus(userId, GoalStatus.ACTIVE))
                .thenReturn(List.of(activeGoal));
        when(getBudgetStatusUseCase.execute(userId, "2026-05", true)).thenReturn(List.of());

        DashboardSummary result = service.execute(userId, "2026-05");

        verify(goalRepository).findByUserIdAndStatus(userId, GoalStatus.ACTIVE);
        assertThat(result.getActiveGoals()).containsExactly(activeGoal);
    }

    // --- fixtures ---

    private Transaction buildTransaction(UUID userId, TransactionType type,
                                         String amount, LocalDate date) {
        Transaction t = new Transaction();
        t.setId(UUID.randomUUID());
        t.setUserId(userId);
        t.setCategoryId(UUID.randomUUID());
        t.setType(type);
        t.setAmount(new BigDecimal(amount));
        t.setDate(date);
        t.setCreatedAt(LocalDateTime.now());
        return t;
    }

    private Goal buildGoal(UUID userId, GoalStatus status) {
        Goal g = new Goal();
        g.setId(UUID.randomUUID());
        g.setUserId(userId);
        g.setTitle("Test Goal");
        g.setTargetAmount(new BigDecimal("1000.00"));
        g.setCurrentAmount(new BigDecimal("200.00"));
        g.setStatus(status);
        g.setCreatedAt(LocalDateTime.now());
        g.setUpdatedAt(LocalDateTime.now());
        return g;
    }
}
