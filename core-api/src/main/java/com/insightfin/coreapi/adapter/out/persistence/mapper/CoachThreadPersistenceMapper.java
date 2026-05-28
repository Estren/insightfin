package com.insightfin.coreapi.adapter.out.persistence.mapper;

import com.insightfin.coreapi.adapter.out.persistence.entity.CoachThreadEntity;
import com.insightfin.coreapi.domain.model.CoachThread;

public class CoachThreadPersistenceMapper {

    private CoachThreadPersistenceMapper() {}

    public static CoachThreadEntity toEntity(CoachThread thread) {
        CoachThreadEntity entity = new CoachThreadEntity();
        entity.setId(thread.getId());
        entity.setUserId(thread.getUserId());
        entity.setFoundryThreadId(thread.getFoundryThreadId());
        entity.setTitle(thread.getTitle());
        entity.setCreatedAt(thread.getCreatedAt());
        entity.setLastMessageAt(thread.getLastMessageAt());
        return entity;
    }

    public static CoachThread toDomain(CoachThreadEntity entity) {
        return new CoachThread(
                entity.getId(),
                entity.getUserId(),
                entity.getFoundryThreadId(),
                entity.getTitle(),
                entity.getCreatedAt(),
                entity.getLastMessageAt()
        );
    }
}
