package com.insightfin.coreapi.application.service;

import com.insightfin.coreapi.domain.exception.DomainException;
import com.insightfin.coreapi.domain.exception.ResourceNotFoundException;
import com.insightfin.coreapi.domain.model.Category;
import com.insightfin.coreapi.domain.model.TransactionType;
import com.insightfin.coreapi.domain.port.in.CreateCategoryUseCase;
import com.insightfin.coreapi.domain.port.in.DeleteCategoryUseCase;
import com.insightfin.coreapi.domain.port.in.ListCategoriesUseCase;
import com.insightfin.coreapi.domain.port.in.UpdateCategoryUseCase;
import com.insightfin.coreapi.domain.port.out.CategoryRepository;
import com.insightfin.coreapi.domain.port.out.RecurringTransactionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CategoryService implements CreateCategoryUseCase, ListCategoriesUseCase,
        UpdateCategoryUseCase, DeleteCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final RecurringTransactionRepository recurringTransactionRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           RecurringTransactionRepository recurringTransactionRepository) {
        this.categoryRepository = categoryRepository;
        this.recurringTransactionRepository = recurringTransactionRepository;
    }

    @Override
    public Category execute(UUID userId, String name, TransactionType type,
                            String icon, String color) {
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setUserId(userId);
        category.setName(name);
        category.setType(type);
        category.setIcon(icon);
        category.setColor(color);
        category.setCreatedAt(LocalDateTime.now());

        return categoryRepository.save(category);
    }

    @Override
    public List<Category> execute(UUID userId, TransactionType type) {
        if (type != null) {
            return categoryRepository.findByUserIdAndType(userId, type);
        }
        return categoryRepository.findByUserId(userId);
    }

    @Override
    public Category execute(UUID userId, UUID categoryId, String name, TransactionType type,
                            String icon, String color) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));

        if (!category.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Category", categoryId);
        }

        category.setName(name);
        category.setType(type);
        category.setIcon(icon);
        category.setColor(color);

        return categoryRepository.save(category);
    }

    @Override
    public void execute(UUID userId, UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));

        if (!category.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Category", categoryId);
        }

        if (categoryRepository.hasTransactions(categoryId)) {
            throw new DomainException("Cannot delete category with existing transactions");
        }

        if (recurringTransactionRepository.countActiveByCategoryId(categoryId) > 0) {
            throw new DomainException("Cannot delete category with active recurring transactions");
        }

        categoryRepository.deleteById(categoryId);
    }
}
