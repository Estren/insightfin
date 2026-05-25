package com.insightfin.coreapi.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        List<TransactionResponse> recentTransactions,
        List<GoalResponse> activeGoals,
        List<BudgetStatusResponse> budgetStatuses
) {}
