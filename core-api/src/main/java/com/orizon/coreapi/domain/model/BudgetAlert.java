package com.orizon.coreapi.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class BudgetAlert {

    private UUID id;
    private UUID userId;
    private UUID budgetId;
    private int thresholdPercentage;
    private BigDecimal amountSpent;
    private BigDecimal budgetAmount;
    private LocalDateTime triggeredAt;
    private boolean read;
    private LocalDateTime createdAt;

    public BudgetAlert() {}

    public BudgetAlert(UUID id, UUID userId, UUID budgetId, int thresholdPercentage,
                       BigDecimal amountSpent, BigDecimal budgetAmount,
                       LocalDateTime triggeredAt, boolean read, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.budgetId = budgetId;
        this.thresholdPercentage = thresholdPercentage;
        this.amountSpent = amountSpent;
        this.budgetAmount = budgetAmount;
        this.triggeredAt = triggeredAt;
        this.read = read;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getBudgetId() { return budgetId; }
    public void setBudgetId(UUID budgetId) { this.budgetId = budgetId; }

    public int getThresholdPercentage() { return thresholdPercentage; }
    public void setThresholdPercentage(int thresholdPercentage) { this.thresholdPercentage = thresholdPercentage; }

    public BigDecimal getAmountSpent() { return amountSpent; }
    public void setAmountSpent(BigDecimal amountSpent) { this.amountSpent = amountSpent; }

    public BigDecimal getBudgetAmount() { return budgetAmount; }
    public void setBudgetAmount(BigDecimal budgetAmount) { this.budgetAmount = budgetAmount; }

    public LocalDateTime getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(LocalDateTime triggeredAt) { this.triggeredAt = triggeredAt; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
