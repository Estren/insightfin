package com.insightfin.coreapi.adapter.out.persistence.repository;

import com.insightfin.coreapi.adapter.out.persistence.entity.AiFeedbackEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class JpaAiFeedbackRepository implements PanacheRepositoryBase<AiFeedbackEntity, UUID> {

    public List<AiFeedbackEntity> findByUserId(UUID userId) {
        return list("userId = ?1 order by createdAt desc", userId);
    }

    public List<AiFeedbackEntity> findByUserIdAndReferenceMonth(UUID userId, String referenceMonth) {
        return list("userId = ?1 and referenceMonth = ?2 order by createdAt desc", userId, referenceMonth);
    }

    public long countUnreadByUserId(UUID userId) {
        return count("userId = ?1 and read = false", userId);
    }

    /**
     * Keyset pagination. The expanded {@code (a < ? OR (a = ? AND b < ?))} predicate
     * is portable across Hibernate dialects (H2 in tests, PostgreSQL in prod) where
     * the row-value comparison sugar {@code (a, b) < (?, ?)} isn't reliably supported.
     */
    public List<AiFeedbackEntity> findPage(UUID userId, LocalDateTime cursorCreatedAt, UUID cursorId, int limit) {
        if (cursorCreatedAt == null) {
            return find("userId = ?1 order by createdAt desc, id desc", userId)
                    .page(Page.ofSize(limit))
                    .list();
        }
        return find("""
                userId = ?1
                  and (createdAt < ?2 or (createdAt = ?2 and id < ?3))
                order by createdAt desc, id desc
                """, userId, cursorCreatedAt, cursorId)
                .page(Page.ofSize(limit))
                .list();
    }
}
