package com.orizon.coreapi.domain.port.out;

import java.util.UUID;

public interface TokenProvider {
    String generateToken(UUID userId, String email);
    UUID extractUserId(String token);
    boolean isValid(String token);
}
