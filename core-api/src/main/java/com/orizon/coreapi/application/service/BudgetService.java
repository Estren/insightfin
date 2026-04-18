package com.orizon.coreapi.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.orizon.coreapi.domain.exception.DuplicateResourceException;
import com.orizon.coreapi.domain.exception.ResourceNotFoundException;
import com.orizon.coreapi.domain.model.Budget;
import com.orizon.coreapi.domain.model.BudgetStatus;
import com.orizon.coreapi.domain.port.in.CreateBudgetUseCase;
import com.orizon.coreapi.domain.port.in.DeleteBudgetUseCase;
import com.orizon.coreapi.domain.port.in.GetBudgetStatusUseCase;
import com.orizon.coreapi.domain.port.in.ListBudgetsUseCase;
import com.orizon.coreapi.domain.port.in.UpdateBudgetUseCase;
import com.orizon.coreapi.domain.port.out.BudgetRepository;
import com.orizon.coreapi.domain.port.out.CategoryRepository;
import com.orizon.coreapi.domain.port.out.TransactionRepository;

public class BudgetService implements CreateBudgetUseCase, ListBudgetsUseCase, GetBudgetStatusUseCase,
        UpdateBudgetUseCase, DeleteBudgetUseCase {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public BudgetService(BudgetRepository budgetRepository,
                         CategoryRepository categoryRepository,
                         TransactionRepository transactionRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Budget execute(UUID userId, UUID categoryId, BigDecimal amount, String month) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));

        budgetRepository.findByUserIdAndCategoryIdAndMonth(userId, categoryId, month)
                .ifPresent(b -> {
                    throw new DuplicateResourceException(
                            "Budget already exists for this category and month");
                });

        Budget budget = new Budget();
        budget.setId(UUID.randomUUID());
        budget.setUserId(userId);
        budget.setCategoryId(categoryId);
        budget.setAmount(amount);
        budget.setMonth(month);
        budget.setCreatedAt(LocalDateTime.now());

        return budgetRepository.save(budget);
    }

    @Override
    public List<Budget> execute(UUID userId, String month) {
        return budgetRepository.findByUserIdAndMonth(userId, month);
    }

    @Override
    public List<BudgetStatus> execute(UUID userId, String month, boolean withStatus) {
        List<Budget> budgets = budgetRepository.findByUserIdAndMonth(userId, month);
        List<BudgetStatus> statuses = new ArrayList<>();

        for (Budget budget : budgets) {
            String categoryName = categoryRepository.findById(budget.getCategoryId())
                    .map(c -> c.getName())
                    .orElse("Unknown");

            BigDecimal spent = transactionRepository.sumAmountByUserIdAndCategoryIdAndMonth(
                    userId, budget.getCategoryId(), month);

            BigDecimal percentage = budget.getAmount().compareTo(BigDecimal.ZERO) > 0
                    ? spent.multiply(BigDecimal.valueOf(100)).divide(budget.getAmount(), 2,
                            RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            statuses.add(new BudgetStatus(
                    budget.getId(), budget.getCategoryId(), categoryName,
                    budget.getAmount(), spent, percentage));
        }

        return statuses;
    }

    @Override
    public Budget execute(UUID userId, UUID budgetId, BigDecimal amount) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", budgetId));

        if (!budget.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Budget", budgetId);
        }

        budget.setAmount(amount);
        return budgetRepository.save(budget);
    }

    @Override
    public void execute(UUID userId, UUID budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", budgetId));

        if (!budget.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Budget", budgetId);
        }

        budgetRepository.deleteById(budgetId);
    }
}
