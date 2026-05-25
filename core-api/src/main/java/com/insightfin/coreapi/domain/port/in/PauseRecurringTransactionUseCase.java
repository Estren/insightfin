package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.RecurringTransaction;

import java.util.UUID;

public interface PauseRecurringTransactionUseCase {
    RecurringTransaction pause(UUID userId, UUID recurringId);
    RecurringTransaction resume(UUID userId, UUID recurringId);
}
