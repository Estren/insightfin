package com.insightfin.coreapi.adapter.in.web.dto;

import com.insightfin.coreapi.domain.model.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTransactionRequest(
        @NotNull UUID categoryId,
        @NotNull TransactionType type,
        @NotNull @Positive BigDecimal amount,
        String description,
        @NotNull LocalDate date
) {}
