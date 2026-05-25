package com.insightfin.coreapi.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.insightfin.coreapi.domain.model.Budget;

public interface BudgetRepository {
    Budget save(Budget budget);
    Optional<Budget> findById(UUID id);
    List<Budget> findByUserIdAndMonth(UUID userId, String month);
    Optional<Budget> findByUserIdAndCategoryIdAndMonth(UUID userId, UUID categoryId, String month);
    void deleteById(UUID id);
}
