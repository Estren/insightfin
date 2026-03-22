package com.orizon.coreapi.adapter.out.persistence.mapper;

import com.orizon.coreapi.adapter.out.persistence.entity.BudgetEntity;
import com.orizon.coreapi.domain.model.Budget;

public class BudgetPersistenceMapper {

    private BudgetPersistenceMapper() {}

    public static BudgetEntity toEntity(Budget budget) {
        BudgetEntity entity = new BudgetEntity();
        entity.setId(budget.getId());
        entity.setUserId(budget.getUserId());
        entity.setCategoryId(budget.getCategoryId());
        entity.setAmount(budget.getAmount());
        entity.setMonth(budget.getMonth());
        entity.setCreatedAt(budget.getCreatedAt());
        return entity;
    }

    public static Budget toDomain(BudgetEntity entity) {
        return new Budget(
                entity.getId(),
                entity.getUserId(),
                entity.getCategoryId(),
                entity.getAmount(),
                entity.getMonth(),
                entity.getCreatedAt()
        );
    }
}
