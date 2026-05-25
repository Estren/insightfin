package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.Transaction;
import com.insightfin.coreapi.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface CreateTransactionUseCase {
    Transaction execute(UUID userId, UUID categoryId, TransactionType type,
                        BigDecimal amount, String description, LocalDate date);
}
