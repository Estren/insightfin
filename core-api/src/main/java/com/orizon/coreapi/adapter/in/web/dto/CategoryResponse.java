package com.orizon.coreapi.adapter.in.web.dto;

import com.orizon.coreapi.domain.model.TransactionType;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        TransactionType type,
        String icon,
        String color,
        LocalDateTime createdAt
) {}
