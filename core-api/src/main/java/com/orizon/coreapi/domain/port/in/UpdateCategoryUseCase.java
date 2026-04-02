package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.Category;
import com.orizon.coreapi.domain.model.TransactionType;

import java.util.UUID;

public interface UpdateCategoryUseCase {
    Category execute(UUID userId, UUID categoryId, String name, TransactionType type,
                     String icon, String color);
}
