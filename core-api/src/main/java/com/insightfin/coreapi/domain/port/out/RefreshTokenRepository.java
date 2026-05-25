package com.insightfin.coreapi.domain.port.out;

import com.insightfin.coreapi.domain.model.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByToken(String token);
    void revokeAllByUserId(UUID userId);
    void revokeByToken(String token);
}
