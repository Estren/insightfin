package com.orizon.coreapi.adapter.out.persistence.repository;

import com.orizon.coreapi.adapter.out.persistence.entity.TransactionEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class JpaTransactionRepository implements PanacheRepositoryBase<TransactionEntity, UUID> {

    public List<TransactionEntity> findByUserIdAndDateBetween(UUID userId, LocalDate startDate, LocalDate endDate) {
        return list("userId = ?1 and date between ?2 and ?3", userId, startDate, endDate);
    }

    public long countByCategoryId(UUID categoryId) {
        return count("categoryId", categoryId);
    }

    public BigDecimal sumAmountByUserIdAndCategoryIdAndDateBetween(UUID userId, UUID categoryId,
                                                                    LocalDate startDate, LocalDate endDate) {
        return getEntityManager()
                .createQuery("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t " +
                        "WHERE t.userId = :userId AND t.categoryId = :categoryId " +
                        "AND t.date >= :startDate AND t.date <= :endDate", BigDecimal.class)
                .setParameter("userId", userId)
                .setParameter("categoryId", categoryId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getSingleResult();
    }
}
