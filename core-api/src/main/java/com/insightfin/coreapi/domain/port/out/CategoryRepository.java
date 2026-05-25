package com.insightfin.coreapi.domain.port.out;

import com.insightfin.coreapi.domain.model.Category;
import com.insightfin.coreapi.domain.model.TransactionType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(UUID id);
    List<Category> findByUserIdAndType(UUID userId, TransactionType type);
    List<Category> findByUserId(UUID userId);
    boolean hasTransactions(UUID categoryId);
    void deleteById(UUID categoryId);
}
