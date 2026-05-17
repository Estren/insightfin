package com.orizon.coreapi.domain.port.out;

import com.orizon.coreapi.domain.model.RecurringTransaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringTransactionRepository {
    RecurringTransaction save(RecurringTransaction recurring);
    Optional<RecurringTransaction> findById(UUID id);
    List<RecurringTransaction> findByUserId(UUID userId);
    List<RecurringTransaction> findDueByDate(LocalDate date);
    long countActiveByCategoryId(UUID categoryId);
    void deleteById(UUID id);
}
