package com.insightfin.coreapi.adapter.out.persistence.mapper;

import com.insightfin.coreapi.adapter.out.persistence.entity.BudgetAlertEntity;
import com.insightfin.coreapi.domain.model.BudgetAlert;

public class BudgetAlertPersistenceMapper {

    private BudgetAlertPersistenceMapper() {}

    public static BudgetAlertEntity toEntity(BudgetAlert alert) {
        var entity = new BudgetAlertEntity();
        entity.setId(alert.getId());
        entity.setUserId(alert.getUserId());
        entity.setBudgetId(alert.getBudgetId());
        entity.setThresholdPercentage(alert.getThresholdPercentage());
        entity.setAmountSpent(alert.getAmountSpent());
        entity.setBudgetAmount(alert.getBudgetAmount());
        entity.setTriggeredAt(alert.getTriggeredAt());
        entity.setRead(alert.isRead());
        entity.setCreatedAt(alert.getCreatedAt());
        return entity;
    }

    public static BudgetAlert toDomain(BudgetAlertEntity entity) {
        return new BudgetAlert(
                entity.getId(),
                entity.getUserId(),
                entity.getBudgetId(),
                entity.getThresholdPercentage(),
                entity.getAmountSpent(),
                entity.getBudgetAmount(),
                entity.getTriggeredAt(),
                entity.isRead(),
                entity.getCreatedAt()
        );
    }
}
