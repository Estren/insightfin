package com.orizon.coreapi.domain.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionCreatedEvent(
        UUID userId,
        UUID transactionId,
        UUID categoryId,
        BigDecimal amount,
        String type,
        LocalDate date) {
}
