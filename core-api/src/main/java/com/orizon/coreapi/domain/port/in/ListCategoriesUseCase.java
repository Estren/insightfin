package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.Category;
import com.orizon.coreapi.domain.model.TransactionType;

import java.util.List;
import java.util.UUID;

public interface ListCategoriesUseCase {
    List<Category> execute(UUID userId, TransactionType type);
}
