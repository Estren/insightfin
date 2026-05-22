package com.orizon.coreapi.adapter.out.persistence;

import com.orizon.coreapi.adapter.out.persistence.mapper.RefreshTokenPersistenceMapper;
import com.orizon.coreapi.adapter.out.persistence.repository.JpaRefreshTokenRepository;
import com.orizon.coreapi.domain.model.RefreshToken;
import com.orizon.coreapi.domain.port.out.RefreshTokenRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    @Inject
    JpaRefreshTokenRepository jpaRefreshTokenRepository;

    @Override
    @Transactional
    public RefreshToken save(RefreshToken refreshToken) {
        var entity = RefreshTokenPersistenceMapper.toEntity(refreshToken);
        jpaRefreshTokenRepository.persist(entity);
        return RefreshTokenPersistenceMapper.toDomain(entity);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRefreshTokenRepository.findByToken(token).map(RefreshTokenPersistenceMapper::toDomain);
    }

    @Override
    @Transactional
    public void revokeAllByUserId(UUID userId) {
        jpaRefreshTokenRepository.revokeAllByUserId(userId);
    }

    @Override
    @Transactional
    public void revokeByToken(String token) {
        jpaRefreshTokenRepository.revokeByToken(token);
    }
}
