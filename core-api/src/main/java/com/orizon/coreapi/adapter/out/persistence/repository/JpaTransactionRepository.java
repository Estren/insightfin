package com.orizon.coreapi.adapter.out.persistence.repository;

import com.orizon.coreapi.adapter.out.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface JpaTransactionRepository extends JpaRepository<TransactionEntity, UUID> {
    List<TransactionEntity> findByUserIdAndDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);
}
