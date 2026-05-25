package com.insightfin.coreapi.adapter.out.persistence.repository;

import com.insightfin.coreapi.adapter.out.persistence.entity.RecurringTransactionEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class JpaRecurringTransactionRepository implements PanacheRepositoryBase<RecurringTransactionEntity, UUID> {

    public List<RecurringTransactionEntity> findByUserId(UUID userId) {
        return list("userId = ?1 order by createdAt desc", userId);
    }

    public List<RecurringTransactionEntity> findDueByDate(LocalDate date) {
        return list("paused = false and nextOccurrence <= ?1 and (endDate is null or endDate >= nextOccurrence)", date);
    }

    public long countActiveByCategoryId(UUID categoryId) {
        return count("categoryId = ?1 and paused = false", categoryId);
    }
}
