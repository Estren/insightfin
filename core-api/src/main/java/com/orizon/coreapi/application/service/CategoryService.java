package com.orizon.coreapi.application.service;

import com.orizon.coreapi.domain.exception.DomainException;
import com.orizon.coreapi.domain.exception.ResourceNotFoundException;
import com.orizon.coreapi.domain.model.Category;
import com.orizon.coreapi.domain.model.TransactionType;
import com.orizon.coreapi.domain.port.in.CreateCategoryUseCase;
import com.orizon.coreapi.domain.port.in.DeleteCategoryUseCase;
import com.orizon.coreapi.domain.port.in.ListCategoriesUseCase;
import com.orizon.coreapi.domain.port.in.UpdateCategoryUseCase;
import com.orizon.coreapi.domain.port.out.CategoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CategoryService implements CreateCategoryUseCase, ListCategoriesUseCase,
        UpdateCategoryUseCase, DeleteCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
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

        categoryRepository.deleteById(categoryId);
    }
}
