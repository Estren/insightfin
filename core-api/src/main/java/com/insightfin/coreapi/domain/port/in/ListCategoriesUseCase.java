package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.Category;
import com.insightfin.coreapi.domain.model.TransactionType;

import java.util.List;
import java.util.UUID;

public interface ListCategoriesUseCase {
    List<Category> execute(UUID userId, TransactionType type);
}
