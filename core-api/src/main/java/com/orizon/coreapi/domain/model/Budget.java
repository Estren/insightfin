package com.orizon.coreapi.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Budget {

    private UUID id;
    private UUID userId;
    private UUID categoryId;
    private BigDecimal amount;
    private String month;
    private LocalDateTime createdAt;

    public Budget() {}

    public Budget(UUID id, UUID userId, UUID categoryId, BigDecimal amount,
                  String month, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.month = month;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
