package com.orizon.coreapi.config.security;

import com.orizon.coreapi.domain.model.Role;
import jakarta.enterprise.context.RequestScoped;

import java.util.UUID;

@RequestScoped
public class AuthenticatedUser {

    private UUID userId;
    private Role role;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
