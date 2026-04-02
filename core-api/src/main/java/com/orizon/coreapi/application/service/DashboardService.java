package com.orizon.coreapi.application.service;

import com.orizon.coreapi.domain.model.*;
import com.orizon.coreapi.domain.port.in.GetBudgetStatusUseCase;
import com.orizon.coreapi.domain.port.in.GetDashboardUseCase;
import com.orizon.coreapi.domain.port.out.GoalRepository;
import com.orizon.coreapi.domain.port.out.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class DashboardService implements GetDashboardUseCase {

    private final TransactionRepository transactionRepository;
    private final GoalRepository goalRepository;
    private final GetBudgetStatusUseCase getBudgetStatusUseCase;

    public DashboardService(TransactionRepository transactionRepository,
                            GoalRepository goalRepository,
                            GetBudgetStatusUseCase getBudgetStatusUseCase) {
        this.transactionRepository = transactionRepository;
        this.goalRepository = goalRepository;
        this.getBudgetStatusUseCase = getBudgetStatusUseCase;
    }

    @Override
    public DashboardSummary execute(UUID userId, String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions = transactionRepository.findByUserIdAndDateBetween(
                userId, startDate, endDate);

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Transaction> recentTransactions = transactions.stream()
                .sorted(Comparator.comparing(Transaction::getDate).reversed())
                .limit(5)
                .toList();

        List<Goal> activeGoals = goalRepository.findByUserIdAndStatus(userId, GoalStatus.ACTIVE);

        List<BudgetStatus> budgetStatuses = getBudgetStatusUseCase.execute(userId, month, true);

        DashboardSummary summary = new DashboardSummary();
        summary.setTotalIncome(totalIncome);
        summary.setTotalExpense(totalExpense);
        summary.setBalance(totalIncome.subtract(totalExpense));
        summary.setRecentTransactions(recentTransactions);
        summary.setActiveGoals(activeGoals);
        summary.setBudgetStatuses(budgetStatuses);

        return summary;
    }
}
