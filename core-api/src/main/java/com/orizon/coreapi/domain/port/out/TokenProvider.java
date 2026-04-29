package com.orizon.coreapi.domain.port.out;

import com.orizon.coreapi.domain.model.Role;

import java.util.UUID;

public interface TokenProvider {
    String generateToken(UUID userId, String email, Role role);
    UUID extractUserId(String token);
    Role extractRole(String token);
    boolean isValid(String token);
}
