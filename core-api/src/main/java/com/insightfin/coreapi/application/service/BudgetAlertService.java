package com.insightfin.coreapi.application.service;

import com.insightfin.coreapi.domain.exception.ResourceNotFoundException;
import com.insightfin.coreapi.domain.model.Budget;
import com.insightfin.coreapi.domain.model.BudgetAlert;
import com.insightfin.coreapi.domain.port.in.EvaluateBudgetAlertsUseCase;
import com.insightfin.coreapi.domain.port.in.ListBudgetAlertsUseCase;
import com.insightfin.coreapi.domain.port.in.MarkBudgetAlertAsReadUseCase;
import com.insightfin.coreapi.domain.port.out.BudgetAlertRepository;
import com.insightfin.coreapi.domain.port.out.BudgetRepository;
import com.insightfin.coreapi.domain.port.out.TransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BudgetAlertService implements EvaluateBudgetAlertsUseCase, ListBudgetAlertsUseCase,
        MarkBudgetAlertAsReadUseCase {

    static final List<Integer> THRESHOLDS = List.of(50, 80, 100);
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final BudgetAlertRepository alertRepository;
    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;

    public BudgetAlertService(BudgetAlertRepository alertRepository,
                              BudgetRepository budgetRepository,
                              TransactionRepository transactionRepository) {
        this.alertRepository = alertRepository;
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public List<BudgetAlert> execute(UUID userId, UUID categoryId, LocalDate transactionDate) {
        String month = transactionDate.format(MONTH_FMT);

        Optional<Budget> budgetOpt = budgetRepository.findByUserIdAndCategoryIdAndMonth(
                userId, categoryId, month);
        if (budgetOpt.isEmpty() || budgetOpt.get().getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }
        Budget budget = budgetOpt.get();

        BigDecimal spent = transactionRepository.sumAmountByUserIdAndCategoryIdAndMonth(
                userId, categoryId, month);

        BigDecimal percentage = spent.multiply(BigDecimal.valueOf(100))
                .divide(budget.getAmount(), 2, RoundingMode.HALF_UP);

        List<BudgetAlert> fired = new ArrayList<>();
        for (int threshold : THRESHOLDS) {
            if (percentage.compareTo(BigDecimal.valueOf(threshold)) >= 0
                    && !alertRepository.existsForBudgetAtThreshold(budget.getId(), threshold)) {
                fired.add(alertRepository.save(buildAlert(userId, budget, threshold, spent)));
            }
        }
        return fired;
    }

    @Override
    public List<BudgetAlert> execute(UUID userId) {
        return alertRepository.findByUserId(userId);
    }

    @Override
    public void execute(UUID userId, UUID alertId) {
        BudgetAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("BudgetAlert", alertId));

        if (!alert.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("BudgetAlert", alertId);
        }

        alert.setRead(true);
        alertRepository.save(alert);
    }

    private BudgetAlert buildAlert(UUID userId, Budget budget, int threshold, BigDecimal spent) {
        LocalDateTime now = LocalDateTime.now();
        BudgetAlert alert = new BudgetAlert();
        alert.setId(UUID.randomUUID());
        alert.setUserId(userId);
        alert.setBudgetId(budget.getId());
        alert.setThresholdPercentage(threshold);
        alert.setAmountSpent(spent);
        alert.setBudgetAmount(budget.getAmount());
        alert.setTriggeredAt(now);
        alert.setRead(false);
        alert.setCreatedAt(now);
        return alert;
    }
}
