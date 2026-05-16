package com.orizon.coreapi.adapter.out.persistence;

import com.orizon.coreapi.adapter.out.persistence.entity.EmailVerificationTokenEntity;
import com.orizon.coreapi.adapter.out.persistence.mapper.EmailVerificationTokenPersistenceMapper;
import com.orizon.coreapi.adapter.out.persistence.repository.JpaEmailVerificationTokenRepository;
import com.orizon.coreapi.domain.model.EmailVerificationPurpose;
import com.orizon.coreapi.domain.model.EmailVerificationToken;
import com.orizon.coreapi.domain.port.out.EmailVerificationTokenRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class EmailVerificationTokenRepositoryAdapter implements EmailVerificationTokenRepository {

    @Inject
    JpaEmailVerificationTokenRepository jpaRepository;

    @Override
    @Transactional
    public EmailVerificationToken save(EmailVerificationToken token) {
        Optional<EmailVerificationTokenEntity> existing = jpaRepository.findByIdOptional(token.getId());
        if (existing.isPresent()) {
            EmailVerificationTokenEntity managed = existing.get();
            managed.setUserId(token.getUserId());
            managed.setTargetEmail(token.getTargetEmail());
            managed.setTokenHash(token.getTokenHash());
            managed.setPinHash(token.getPinHash());
            managed.setPinAttempts(token.getPinAttempts());
            managed.setPurpose(token.getPurpose());
            managed.setExpiresAt(token.getExpiresAt());
            managed.setUsedAt(token.getUsedAt());
            return EmailVerificationTokenPersistenceMapper.toDomain(managed);
        }
        EmailVerificationTokenEntity entity = EmailVerificationTokenPersistenceMapper.toEntity(token);
        jpaRepository.persist(entity);
        return EmailVerificationTokenPersistenceMapper.toDomain(entity);
    }

    @Override
    public Optional<EmailVerificationToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(EmailVerificationTokenPersistenceMapper::toDomain);
    }

    @Override
    public Optional<EmailVerificationToken> findActiveByUserAndPurpose(UUID userId, EmailVerificationPurpose purpose) {
        return jpaRepository.findActiveByUserAndPurpose(userId, purpose).map(EmailVerificationTokenPersistenceMapper::toDomain);
    }

    @Override
    @Transactional
    public void invalidateActiveByUserAndPurpose(UUID userId, EmailVerificationPurpose purpose) {
        jpaRepository.invalidateActiveByUserAndPurpose(userId, purpose, LocalDateTime.now());
    }

    @Override
    public Optional<EmailVerificationToken> findActiveByTargetEmailAndPurpose(String targetEmail, EmailVerificationPurpose purpose) {
        return jpaRepository.findActiveByTargetEmailAndPurpose(targetEmail, purpose)
                .map(EmailVerificationTokenPersistenceMapper::toDomain);
    }
}
