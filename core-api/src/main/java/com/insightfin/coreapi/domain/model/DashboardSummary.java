package com.insightfin.coreapi.domain.model;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSummary {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
    private List<Transaction> recentTransactions;
    private List<Goal> activeGoals;
    private List<BudgetStatus> budgetStatuses;

    public DashboardSummary() {}

    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public List<Transaction> getRecentTransactions() { return recentTransactions; }
    public void setRecentTransactions(List<Transaction> recentTransactions) { this.recentTransactions = recentTransactions; }

    public List<Goal> getActiveGoals() { return activeGoals; }
    public void setActiveGoals(List<Goal> activeGoals) { this.activeGoals = activeGoals; }

    public List<BudgetStatus> getBudgetStatuses() { return budgetStatuses; }
    public void setBudgetStatuses(List<BudgetStatus> budgetStatuses) { this.budgetStatuses = budgetStatuses; }
}
