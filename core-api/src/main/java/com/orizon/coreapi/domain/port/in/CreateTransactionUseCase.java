package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.Transaction;
import com.orizon.coreapi.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface CreateTransactionUseCase {
    Transaction execute(UUID userId, UUID categoryId, TransactionType type,
                        BigDecimal amount, String description, LocalDate date);
}
