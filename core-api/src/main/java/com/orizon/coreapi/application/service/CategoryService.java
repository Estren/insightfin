package com.orizon.coreapi.application.service;

import com.orizon.coreapi.domain.model.Category;
import com.orizon.coreapi.domain.model.TransactionType;
import com.orizon.coreapi.domain.port.in.CreateCategoryUseCase;
import com.orizon.coreapi.domain.port.in.ListCategoriesUseCase;
import com.orizon.coreapi.domain.port.out.CategoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CategoryService implements CreateCategoryUseCase, ListCategoriesUseCase {

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
}
