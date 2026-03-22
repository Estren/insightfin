package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.Category;
import com.orizon.coreapi.domain.model.TransactionType;

import java.util.UUID;

public interface CreateCategoryUseCase {
    Category execute(UUID userId, String name, TransactionType type, String icon, String color);
}
