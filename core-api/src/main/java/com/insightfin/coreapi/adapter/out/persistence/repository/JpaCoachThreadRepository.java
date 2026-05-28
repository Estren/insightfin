package com.insightfin.coreapi.adapter.out.persistence.repository;

import com.insightfin.coreapi.adapter.out.persistence.entity.CoachThreadEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class JpaCoachThreadRepository implements PanacheRepositoryBase<CoachThreadEntity, UUID> {

    public List<CoachThreadEntity> findByUserIdOrderByLastMessageDesc(UUID userId) {
        return list("userId = ?1", Sort.by("lastMessageAt").descending(), userId);
    }
}
