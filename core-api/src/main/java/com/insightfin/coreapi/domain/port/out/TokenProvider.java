package com.insightfin.coreapi.domain.port.out;

import com.insightfin.coreapi.domain.model.Role;

import java.util.UUID;

public interface TokenProvider {
    String generateToken(UUID userId, String email, Role role, boolean emailVerified);
    UUID extractUserId(String token);
    Role extractRole(String token);
    boolean isEmailVerified(String token);
    boolean isValid(String token);
}
