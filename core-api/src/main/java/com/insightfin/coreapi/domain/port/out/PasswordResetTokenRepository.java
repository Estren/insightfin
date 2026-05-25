package com.insightfin.coreapi.domain.port.out;

import com.insightfin.coreapi.domain.model.PasswordResetToken;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository {
    PasswordResetToken save(PasswordResetToken token);
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    void invalidateAllByUserId(UUID userId);
}
