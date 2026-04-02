package com.orizon.coreapi.adapter.out.persistence.repository;

import com.orizon.coreapi.adapter.out.persistence.entity.RefreshTokenEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class JpaRefreshTokenRepository implements PanacheRepositoryBase<RefreshTokenEntity, UUID> {

    public Optional<RefreshTokenEntity> findByToken(String token) {
        return find("token", token).firstResultOptional();
    }

    public int revokeAllByUserId(UUID userId) {
        return update("revoked = true where userId = ?1 and revoked = false", userId);
    }
}
