package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.RecurrenceFrequency;
import com.insightfin.coreapi.domain.model.RecurringTransaction;
import com.insightfin.coreapi.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface UpdateRecurringTransactionUseCase {
    RecurringTransaction update(UUID userId, UUID recurringId, UUID categoryId, TransactionType type,
                                BigDecimal amount, String description,
                                RecurrenceFrequency frequency,
                                LocalDate startDate, LocalDate endDate);
}
