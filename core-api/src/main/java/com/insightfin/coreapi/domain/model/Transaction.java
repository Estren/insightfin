package com.insightfin.coreapi.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {

    private UUID id;
    private UUID userId;
    private UUID categoryId;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private LocalDate date;
    private UUID recurringTransactionId;
    private LocalDateTime createdAt;

    public Transaction() {}

    public Transaction(UUID id, UUID userId, UUID categoryId, TransactionType type,
                       BigDecimal amount, String description, LocalDate date,
                       LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public UUID getRecurringTransactionId() { return recurringTransactionId; }
    public void setRecurringTransactionId(UUID recurringTransactionId) { this.recurringTransactionId = recurringTransactionId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
