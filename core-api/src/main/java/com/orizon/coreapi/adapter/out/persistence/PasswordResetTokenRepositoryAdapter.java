package com.orizon.coreapi.adapter.out.persistence;

import com.orizon.coreapi.adapter.out.persistence.entity.PasswordResetTokenEntity;
import com.orizon.coreapi.adapter.out.persistence.mapper.PasswordResetTokenPersistenceMapper;
import com.orizon.coreapi.adapter.out.persistence.repository.JpaPasswordResetTokenRepository;
import com.orizon.coreapi.domain.model.PasswordResetToken;
import com.orizon.coreapi.domain.port.out.PasswordResetTokenRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepository {

    @Inject
    JpaPasswordResetTokenRepository jpaRepository;

    @Override
    @Transactional
    public PasswordResetToken save(PasswordResetToken token) {
        Optional<PasswordResetTokenEntity> existing = jpaRepository.findByIdOptional(token.getId());
        if (existing.isPresent()) {
            PasswordResetTokenEntity managed = existing.get();
            managed.setUserId(token.getUserId());
            managed.setTokenHash(token.getTokenHash());
            managed.setExpiresAt(token.getExpiresAt());
            managed.setUsedAt(token.getUsedAt());
            return PasswordResetTokenPersistenceMapper.toDomain(managed);
        }
        var entity = PasswordResetTokenPersistenceMapper.toEntity(token);
        jpaRepository.persist(entity);
        return PasswordResetTokenPersistenceMapper.toDomain(entity);
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(PasswordResetTokenPersistenceMapper::toDomain);
    }

    @Override
    @Transactional
    public void invalidateAllByUserId(UUID userId) {
        jpaRepository.invalidateAllByUserId(userId, LocalDateTime.now());
    }
}
