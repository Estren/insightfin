package com.orizon.coreapi.domain.port.out;

import com.orizon.coreapi.domain.model.EmailVerificationPurpose;
import com.orizon.coreapi.domain.model.EmailVerificationToken;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository {
    EmailVerificationToken save(EmailVerificationToken token);
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);
    Optional<EmailVerificationToken> findActiveByUserAndPurpose(UUID userId, EmailVerificationPurpose purpose);
    void invalidateActiveByUserAndPurpose(UUID userId, EmailVerificationPurpose purpose);
    Optional<EmailVerificationToken> findActiveByTargetEmailAndPurpose(String targetEmail, EmailVerificationPurpose purpose);
}
