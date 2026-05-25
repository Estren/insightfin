package com.insightfin.coreapi.application.service;

import com.insightfin.coreapi.application.pagination.Cursor;
import com.insightfin.coreapi.application.pagination.Page;
import com.insightfin.coreapi.application.pagination.PaginationParams;
import com.insightfin.coreapi.domain.model.AiFeedback;
import com.insightfin.coreapi.domain.model.AiFeedbackNotification;
import com.insightfin.coreapi.domain.model.BudgetAlert;
import com.insightfin.coreapi.domain.model.BudgetAlertNotification;
import com.insightfin.coreapi.domain.model.Notification;
import com.insightfin.coreapi.domain.model.UnreadCounts;
import com.insightfin.coreapi.domain.port.in.GetUnreadCountsUseCase;
import com.insightfin.coreapi.domain.port.in.ListNotificationsUseCase;
import com.insightfin.coreapi.domain.port.out.AiFeedbackRepository;
import com.insightfin.coreapi.domain.port.out.BudgetAlertRepository;
import com.insightfin.coreapi.domain.port.out.BudgetRepository;
import com.insightfin.coreapi.domain.port.out.CategoryRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class NotificationService implements ListNotificationsUseCase, GetUnreadCountsUseCase {

    private final AiFeedbackRepository aiFeedbackRepository;
    private final BudgetAlertRepository budgetAlertRepository;
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;

    public NotificationService(AiFeedbackRepository aiFeedbackRepository,
                               BudgetAlertRepository budgetAlertRepository,
                               BudgetRepository budgetRepository,
                               CategoryRepository categoryRepository) {
        this.aiFeedbackRepository = aiFeedbackRepository;
        this.budgetAlertRepository = budgetAlertRepository;
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Page<Notification> list(UUID userId, PaginationParams params) {
        // Push the cursor into each repo so the DB seeks instead of scanning.
        // Each side fetches up to limit+1 of its own table — the merge needs at
        // most 2*(limit+1) candidates to be guaranteed to contain the global top
        // limit+1. We then sort, take limit+1, and let Page handle the hasMore trick.
        LocalDateTime cursorAt = params.cursor() == null ? null : params.cursor().createdAt();
        UUID cursorId = params.cursor() == null ? null : params.cursor().id();
        int fetchSize = params.limit() + 1;

        List<AiFeedback> feedbacks = aiFeedbackRepository.findPage(userId, cursorAt, cursorId, fetchSize);
        List<BudgetAlert> alerts = budgetAlertRepository.findPage(userId, cursorAt, cursorId, fetchSize);

        Map<UUID, String> categoryNames = resolveCategoryNames(alerts);

        List<Notification> merged = Stream.concat(
                        feedbacks.stream().map(f -> (Notification) new AiFeedbackNotification(f)),
                        alerts.stream().map(a -> (Notification) new BudgetAlertNotification(
                                a, categoryNames.getOrDefault(a.getBudgetId(), "Unknown"))))
                .sorted(Comparator.comparing(Notification::createdAt).reversed()
                        .thenComparing(Notification::id, Comparator.reverseOrder()))
                .limit(fetchSize)
                .toList();

        return Page.fromOversizedList(merged, params.limit(),
                n -> new Cursor(n.createdAt(), n.id()));
    }

    @Override
    public UnreadCounts count(UUID userId) {
        return new UnreadCounts(
                aiFeedbackRepository.countUnreadByUserId(userId),
                budgetAlertRepository.countUnreadByUserId(userId));
    }

    /**
     * Returns a map from {@code budgetId} to the budget's category name. Looks up
     * each distinct budget once (avoids N+1 over the alerts list).
     */
    private Map<UUID, String> resolveCategoryNames(List<BudgetAlert> alerts) {
        Map<UUID, String> result = new HashMap<>();
        for (BudgetAlert alert : alerts) {
            if (result.containsKey(alert.getBudgetId())) {
                continue;
            }
            String name = budgetRepository.findById(alert.getBudgetId())
                    .flatMap(b -> categoryRepository.findById(b.getCategoryId()))
                    .map(c -> c.getName())
                    .orElse("Unknown");
            result.put(alert.getBudgetId(), name);
        }
        return result;
    }
}
