package com.orizon.coreapi.domain.port.out;

import com.orizon.coreapi.domain.model.Budget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository {
    Budget save(Budget budget);
    Optional<Budget> findById(UUID id);
    List<Budget> findByUserIdAndMonth(UUID userId, String month);
    Optional<Budget> findByUserIdAndCategoryIdAndMonth(UUID userId, UUID categoryId, String month);
}
