package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.Category;
import com.insightfin.coreapi.domain.model.TransactionType;

import java.util.UUID;

public interface CreateCategoryUseCase {
    Category execute(UUID userId, String name, TransactionType type, String icon, String color);
}
