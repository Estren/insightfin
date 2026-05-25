package com.insightfin.coreapi.adapter.out.persistence.mapper;

import com.insightfin.coreapi.adapter.out.persistence.entity.CategoryEntity;
import com.insightfin.coreapi.domain.model.Category;
import com.insightfin.coreapi.domain.model.TransactionType;

public class CategoryPersistenceMapper {

    private CategoryPersistenceMapper() {}

    public static CategoryEntity toEntity(Category category) {
        CategoryEntity entity = new CategoryEntity();
        entity.setId(category.getId());
        entity.setUserId(category.getUserId());
        entity.setName(category.getName());
        entity.setType(category.getType().name());
        entity.setIcon(category.getIcon());
        entity.setColor(category.getColor());
        entity.setCreatedAt(category.getCreatedAt());
        return entity;
    }

    public static Category toDomain(CategoryEntity entity) {
        return new Category(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                TransactionType.valueOf(entity.getType()),
                entity.getIcon(),
                entity.getColor(),
                entity.getCreatedAt()
        );
    }
}
