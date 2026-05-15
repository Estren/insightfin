package com.orizon.coreapi.adapter.out.persistence.mapper;

import com.orizon.coreapi.adapter.out.persistence.entity.PasswordResetTokenEntity;
import com.orizon.coreapi.domain.model.PasswordResetToken;

public class PasswordResetTokenPersistenceMapper {

    private PasswordResetTokenPersistenceMapper() {}

    public static PasswordResetTokenEntity toEntity(PasswordResetToken token) {
        var entity = new PasswordResetTokenEntity();
        entity.setId(token.getId());
        entity.setUserId(token.getUserId());
        entity.setTokenHash(token.getTokenHash());
        entity.setExpiresAt(token.getExpiresAt());
        entity.setUsedAt(token.getUsedAt());
        entity.setCreatedAt(token.getCreatedAt());
        return entity;
    }

    public static PasswordResetToken toDomain(PasswordResetTokenEntity entity) {
        return new PasswordResetToken(
                entity.getId(),
                entity.getUserId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getUsedAt(),
                entity.getCreatedAt()
        );
    }
}
