package com.orizon.coreapi.domain.port.out;

import com.orizon.coreapi.domain.model.Category;
import com.orizon.coreapi.domain.model.TransactionType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(UUID id);
    List<Category> findByUserIdAndType(UUID userId, TransactionType type);
    List<Category> findByUserId(UUID userId);
}
