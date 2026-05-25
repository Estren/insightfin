package com.insightfin.coreapi.adapter.out.persistence.mapper;

import com.insightfin.coreapi.adapter.out.persistence.entity.EmailVerificationTokenEntity;
import com.insightfin.coreapi.domain.model.EmailVerificationToken;

public class EmailVerificationTokenPersistenceMapper {

    private EmailVerificationTokenPersistenceMapper() {}

    public static EmailVerificationTokenEntity toEntity(EmailVerificationToken token) {
        EmailVerificationTokenEntity entity = new EmailVerificationTokenEntity();
        entity.setId(token.getId());
        entity.setUserId(token.getUserId());
        entity.setTargetEmail(token.getTargetEmail());
        entity.setTokenHash(token.getTokenHash());
        entity.setPinHash(token.getPinHash());
        entity.setPinAttempts(token.getPinAttempts());
        entity.setPurpose(token.getPurpose());
        entity.setExpiresAt(token.getExpiresAt());
        entity.setUsedAt(token.getUsedAt());
        entity.setCreatedAt(token.getCreatedAt());
        return entity;
    }

    public static EmailVerificationToken toDomain(EmailVerificationTokenEntity entity) {
        return new EmailVerificationToken(
                entity.getId(),
                entity.getUserId(),
                entity.getTargetEmail(),
                entity.getTokenHash(),
                entity.getPinHash(),
                entity.getPinAttempts(),
                entity.getPurpose(),
                entity.getExpiresAt(),
                entity.getUsedAt(),
                entity.getCreatedAt()
        );
    }
}
