package com.orizon.coreapi.config.security;

import jakarta.enterprise.context.RequestScoped;

import java.util.UUID;

@RequestScoped
public class AuthenticatedUser {

    private UUID userId;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
