package com.orizon.coreapi.adapter.out.persistence.repository;

import com.orizon.coreapi.adapter.out.persistence.entity.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaBudgetRepository extends JpaRepository<BudgetEntity, UUID> {
    List<BudgetEntity> findByUserIdAndMonth(UUID userId, String month);
    Optional<BudgetEntity> findByUserIdAndCategoryIdAndMonth(UUID userId, UUID categoryId, String month);
}
