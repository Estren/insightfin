package com.orizon.coreapi.adapter.in.web.mapper;

import com.orizon.coreapi.adapter.in.web.dto.*;
import com.orizon.coreapi.domain.model.*;

import java.util.Map;
import java.util.UUID;

public class WebMapper {

    private WebMapper() {}

    public static UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getCreatedAt(), user.getAvatarUrl());
    }

    public static TransactionResponse toResponse(Transaction transaction, String categoryName) {
        return new TransactionResponse(
                transaction.getId(), transaction.getCategoryId(), categoryName,
                transaction.getType(), transaction.getAmount(), transaction.getDescription(),
                transaction.getDate(), transaction.getRecurringTransactionId(), transaction.getCreatedAt());
    }

    public static RecurringTransactionResponse toResponse(RecurringTransaction r, String categoryName) {
        return new RecurringTransactionResponse(
                r.getId(), r.getCategoryId(), categoryName,
                r.getType(), r.getAmount(), r.getDescription(),
                r.getFrequency(), r.getStartDate(), r.getEndDate(),
                r.getNextOccurrence(), r.getLastGeneratedAt(),
                r.isPaused(), r.getCreatedAt());
    }

    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(), category.getName(), category.getType(),
                category.getIcon(), category.getColor(), category.getCreatedAt());
    }

    public static GoalResponse toResponse(Goal goal) {
        return new GoalResponse(
                goal.getId(), goal.getTitle(), goal.getTargetAmount(),
                goal.getCurrentAmount(), goal.getDeadline(),
                goal.getStatus(), goal.getCreatedAt());
    }

    public static GoalContributionResponse toResponse(GoalContribution contribution) {
        return new GoalContributionResponse(
                contribution.getId(), contribution.getGoalId(),
                contribution.getAmount(), contribution.getDate(), contribution.getCreatedAt());
    }

    public static BudgetResponse toResponse(Budget budget, String categoryName) {
        return new BudgetResponse(
                budget.getId(), budget.getCategoryId(), categoryName,
                budget.getAmount(), budget.getMonth(), budget.getCreatedAt());
    }

    public static BudgetStatusResponse toResponse(BudgetStatus status) {
        return new BudgetStatusResponse(
                status.getBudgetId(), status.getCategoryId(), status.getCategoryName(),
                status.getBudgetAmount(), status.getSpentAmount(), status.getPercentageUsed());
    }

    public static AiFeedbackResponse toResponse(AiFeedback feedback) {
        return new AiFeedbackResponse(
                feedback.getId(), feedback.getType(), feedback.getTitle(),
                feedback.getContent(), feedback.getMetadata(), feedback.getReferenceMonth(),
                feedback.isRead(), feedback.getCreatedAt());
    }

    public static DashboardResponse toResponse(DashboardSummary summary, Map<UUID, String> categoryNames) {
        return new DashboardResponse(
                summary.getTotalIncome(),
                summary.getTotalExpense(),
                summary.getBalance(),
                summary.getRecentTransactions().stream()
                        .map(t -> toResponse(t, categoryNames.getOrDefault(t.getCategoryId(), "Unknown")))
                        .toList(),
                summary.getActiveGoals().stream().map(WebMapper::toResponse).toList(),
                summary.getBudgetStatuses().stream().map(WebMapper::toResponse).toList());
    }
}
