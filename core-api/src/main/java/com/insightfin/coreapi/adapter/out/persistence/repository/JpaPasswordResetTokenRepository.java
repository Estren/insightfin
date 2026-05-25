package com.insightfin.coreapi.adapter.out.persistence.repository;

import com.insightfin.coreapi.adapter.out.persistence.entity.PasswordResetTokenEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class JpaPasswordResetTokenRepository implements PanacheRepositoryBase<PasswordResetTokenEntity, UUID> {

    public Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash) {
        return find("tokenHash", tokenHash).firstResultOptional();
    }

    public int invalidateAllByUserId(UUID userId, LocalDateTime now) {
        return update("usedAt = ?1 where userId = ?2 and usedAt is null", now, userId);
    }
}
