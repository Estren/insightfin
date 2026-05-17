package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.RecurringTransaction;

import java.util.UUID;

public interface GetRecurringTransactionUseCase {
    RecurringTransaction getById(UUID userId, UUID recurringId);
}
