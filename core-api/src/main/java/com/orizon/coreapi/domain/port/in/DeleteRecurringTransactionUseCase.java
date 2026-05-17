package com.orizon.coreapi.domain.port.in;

import java.util.UUID;

public interface DeleteRecurringTransactionUseCase {
    void delete(UUID userId, UUID recurringId);
}
