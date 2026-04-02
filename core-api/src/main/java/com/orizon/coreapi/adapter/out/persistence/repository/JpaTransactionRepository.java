package com.orizon.coreapi.adapter.out.persistence.repository;

import com.orizon.coreapi.adapter.out.persistence.entity.TransactionEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class JpaTransactionRepository implements PanacheRepositoryBase<TransactionEntity, UUID> {

    public List<TransactionEntity> findByUserIdAndDateBetween(UUID userId, LocalDate startDate, LocalDate endDate) {
        return list("userId = ?1 and date between ?2 and ?3", userId, startDate, endDate);
    }
}
