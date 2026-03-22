package com.orizon.coreapi.adapter.in.web.dto;

import com.orizon.coreapi.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID categoryId,
        TransactionType type,
        BigDecimal amount,
        String description,
        LocalDate date,
        LocalDateTime createdAt
) {}
