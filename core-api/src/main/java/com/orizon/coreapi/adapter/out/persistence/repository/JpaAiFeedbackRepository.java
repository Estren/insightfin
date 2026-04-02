package com.orizon.coreapi.adapter.out.persistence.repository;

import com.orizon.coreapi.adapter.out.persistence.entity.AiFeedbackEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class JpaAiFeedbackRepository implements PanacheRepositoryBase<AiFeedbackEntity, UUID> {

    public List<AiFeedbackEntity> findByUserId(UUID userId) {
        return list("userId order by createdAt desc", userId);
    }

    public List<AiFeedbackEntity> findByUserIdAndReferenceMonth(UUID userId, String referenceMonth) {
        return list("userId = ?1 and referenceMonth = ?2 order by createdAt desc", userId, referenceMonth);
    }
}
