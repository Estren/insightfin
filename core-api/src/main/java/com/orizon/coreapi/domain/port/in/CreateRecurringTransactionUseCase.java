package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.RecurrenceFrequency;
import com.orizon.coreapi.domain.model.RecurringTransaction;
import com.orizon.coreapi.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface CreateRecurringTransactionUseCase {
    RecurringTransaction create(UUID userId, UUID categoryId, TransactionType type,
                                BigDecimal amount, String description,
                                RecurrenceFrequency frequency,
                                LocalDate startDate, LocalDate endDate);
}
