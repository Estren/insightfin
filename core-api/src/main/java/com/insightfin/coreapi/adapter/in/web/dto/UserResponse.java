package com.insightfin.coreapi.adapter.in.web.dto;

import com.insightfin.coreapi.domain.model.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        Role role,
        LocalDateTime createdAt,
        String avatarUrl,
        boolean hasPassword,
        boolean linkedWithGoogle
) {}
