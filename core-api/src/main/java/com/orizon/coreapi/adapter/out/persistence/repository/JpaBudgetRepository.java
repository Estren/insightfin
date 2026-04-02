package com.orizon.coreapi.adapter.out.persistence.repository;

import com.orizon.coreapi.adapter.out.persistence.entity.BudgetEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class JpaBudgetRepository implements PanacheRepositoryBase<BudgetEntity, UUID> {

    public List<BudgetEntity> findByUserIdAndMonth(UUID userId, String month) {
        return list("userId = ?1 and month = ?2", userId, month);
    }

    public Optional<BudgetEntity> findByUserIdAndCategoryIdAndMonth(UUID userId, UUID categoryId, String month) {
        return find("userId = ?1 and categoryId = ?2 and month = ?3", userId, categoryId, month).firstResultOptional();
    }
}
