package com.insightfin.coreapi.domain.port.in;

import java.util.UUID;

public interface DeleteCategoryUseCase {
    void execute(UUID userId, UUID categoryId);
}
