package com.orizon.coreapi.application.service;

import com.orizon.coreapi.domain.exception.DuplicateResourceException;
import com.orizon.coreapi.domain.exception.ResourceNotFoundException;
import com.orizon.coreapi.domain.model.Budget;
import com.orizon.coreapi.domain.port.in.CreateBudgetUseCase;
import com.orizon.coreapi.domain.port.in.ListBudgetsUseCase;
import com.orizon.coreapi.domain.port.out.BudgetRepository;
import com.orizon.coreapi.domain.port.out.CategoryRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BudgetService implements CreateBudgetUseCase, ListBudgetsUseCase {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;

    public BudgetService(BudgetRepository budgetRepository,
                         CategoryRepository categoryRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
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
}
