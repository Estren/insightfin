package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.RecurringTransaction;

import java.util.UUID;

public interface GetRecurringTransactionUseCase {
    RecurringTransaction getById(UUID userId, UUID recurringId);
}
