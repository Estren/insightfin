package com.insightfin.coreapi.adapter.out.persistence.mapper;

import com.insightfin.coreapi.adapter.out.persistence.entity.AiFeedbackEntity;
import com.insightfin.coreapi.domain.model.AiFeedback;
import com.insightfin.coreapi.domain.model.AiFeedbackType;

public class AiFeedbackPersistenceMapper {

    private AiFeedbackPersistenceMapper() {}

    public static AiFeedbackEntity toEntity(AiFeedback feedback) {
        var entity = new AiFeedbackEntity();
        entity.setId(feedback.getId());
        entity.setUserId(feedback.getUserId());
        entity.setType(feedback.getType().name());
        entity.setTitle(feedback.getTitle());
        entity.setContent(feedback.getContent());
        entity.setMetadata(feedback.getMetadata());
        entity.setReferenceMonth(feedback.getReferenceMonth());
        entity.setRead(feedback.isRead());
        entity.setCreatedAt(feedback.getCreatedAt());
        return entity;
    }

    public static AiFeedback toDomain(AiFeedbackEntity entity) {
        return new AiFeedback(
                entity.getId(),
                entity.getUserId(),
                AiFeedbackType.valueOf(entity.getType()),
                entity.getTitle(),
                entity.getContent(),
                entity.getMetadata(),
                entity.getReferenceMonth(),
                entity.isRead(),
                entity.getCreatedAt()
        );
    }
}
