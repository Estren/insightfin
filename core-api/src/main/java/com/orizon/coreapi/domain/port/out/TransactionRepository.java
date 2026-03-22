package com.orizon.coreapi.domain.port.out;

import com.orizon.coreapi.domain.model.Transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(UUID id);
    List<Transaction> findByUserIdAndDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);
    void deleteById(UUID id);
}
