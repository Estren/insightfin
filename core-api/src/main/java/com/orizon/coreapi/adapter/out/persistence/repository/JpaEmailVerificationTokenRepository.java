package com.orizon.coreapi.adapter.out.persistence.repository;

import com.orizon.coreapi.adapter.out.persistence.entity.EmailVerificationTokenEntity;
import com.orizon.coreapi.domain.model.EmailVerificationPurpose;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class JpaEmailVerificationTokenRepository implements PanacheRepositoryBase<EmailVerificationTokenEntity, UUID> {

    public Optional<EmailVerificationTokenEntity> findByTokenHash(String tokenHash) {
        return find("tokenHash", tokenHash).firstResultOptional();
    }

    public Optional<EmailVerificationTokenEntity> findActiveByUserAndPurpose(UUID userId, EmailVerificationPurpose purpose) {
        return find("userId = ?1 and purpose = ?2 and usedAt is null", userId, purpose).firstResultOptional();
    }

    public int invalidateActiveByUserAndPurpose(UUID userId, EmailVerificationPurpose purpose, LocalDateTime now) {
        return update("usedAt = ?1 where userId = ?2 and purpose = ?3 and usedAt is null", now, userId, purpose);
    }
}
