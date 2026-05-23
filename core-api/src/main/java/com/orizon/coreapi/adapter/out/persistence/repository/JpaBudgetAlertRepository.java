package com.orizon.coreapi.adapter.out.persistence.repository;

import com.orizon.coreapi.adapter.out.persistence.entity.BudgetAlertEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class JpaBudgetAlertRepository implements PanacheRepositoryBase<BudgetAlertEntity, UUID> {

    public List<BudgetAlertEntity> findByUserId(UUID userId) {
        return list("userId = ?1 order by triggeredAt desc", userId);
    }

    public boolean existsByBudgetIdAndThreshold(UUID budgetId, int thresholdPercentage) {
        return count("budgetId = ?1 and thresholdPercentage = ?2", budgetId, thresholdPercentage) > 0;
    }
}
