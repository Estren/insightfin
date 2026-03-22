package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.Transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ListTransactionsUseCase {
    List<Transaction> execute(UUID userId, LocalDate startDate, LocalDate endDate);
}
