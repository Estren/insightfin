package com.insightfin.coreapi.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class GoalContribution {

    private UUID id;
    private UUID goalId;
    private BigDecimal amount;
    private LocalDate date;
    private LocalDateTime createdAt;

    public GoalContribution() {}

    public GoalContribution(UUID id, UUID goalId, BigDecimal amount,
                            LocalDate date, LocalDateTime createdAt) {
        this.id = id;
        this.goalId = goalId;
        this.amount = amount;
        this.date = date;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getGoalId() { return goalId; }
    public void setGoalId(UUID goalId) { this.goalId = goalId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
