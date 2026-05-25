package com.insightfin.coreapi.adapter.in.web.dto;

import com.insightfin.coreapi.domain.model.RecurrenceFrequency;
import com.insightfin.coreapi.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecurringTransactionResponse(
        UUID id,
        UUID categoryId,
        String categoryName,
        TransactionType type,
        BigDecimal amount,
        String description,
        RecurrenceFrequency frequency,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate nextOccurrence,
        LocalDate lastGeneratedAt,
        boolean paused,
        LocalDateTime createdAt
) {}
