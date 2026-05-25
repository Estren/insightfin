package com.insightfin.coreapi.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class BudgetStatus {

    private UUID budgetId;
    private UUID categoryId;
    private String categoryName;
    private BigDecimal budgetAmount;
    private BigDecimal spentAmount;
    private BigDecimal percentageUsed;

    public BudgetStatus() {}

    public BudgetStatus(UUID budgetId, UUID categoryId, String categoryName,
                        BigDecimal budgetAmount, BigDecimal spentAmount, BigDecimal percentageUsed) {
        this.budgetId = budgetId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.budgetAmount = budgetAmount;
        this.spentAmount = spentAmount;
        this.percentageUsed = percentageUsed;
    }

    public UUID getBudgetId() { return budgetId; }
    public void setBudgetId(UUID budgetId) { this.budgetId = budgetId; }

    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public BigDecimal getBudgetAmount() { return budgetAmount; }
    public void setBudgetAmount(BigDecimal budgetAmount) { this.budgetAmount = budgetAmount; }

    public BigDecimal getSpentAmount() { return spentAmount; }
    public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }

    public BigDecimal getPercentageUsed() { return percentageUsed; }
    public void setPercentageUsed(BigDecimal percentageUsed) { this.percentageUsed = percentageUsed; }
}
