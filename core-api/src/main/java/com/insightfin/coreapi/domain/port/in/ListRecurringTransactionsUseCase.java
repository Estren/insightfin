package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.RecurringTransaction;

import java.util.List;
import java.util.UUID;

public interface ListRecurringTransactionsUseCase {
    List<RecurringTransaction> list(UUID userId);
}
