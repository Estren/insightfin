package com.orizon.coreapi.application.service;

import com.orizon.coreapi.domain.model.AiFeedback;
import com.orizon.coreapi.domain.model.AiFeedbackNotification;
import com.orizon.coreapi.domain.model.BudgetAlert;
import com.orizon.coreapi.domain.model.BudgetAlertNotification;
import com.orizon.coreapi.domain.model.Notification;
import com.orizon.coreapi.domain.model.UnreadCounts;
import com.orizon.coreapi.domain.port.in.GetUnreadCountsUseCase;
import com.orizon.coreapi.domain.port.in.ListNotificationsUseCase;
import com.orizon.coreapi.domain.port.out.AiFeedbackRepository;
import com.orizon.coreapi.domain.port.out.BudgetAlertRepository;
import com.orizon.coreapi.domain.port.out.BudgetRepository;
import com.orizon.coreapi.domain.port.out.CategoryRepository;

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
    public List<Notification> list(UUID userId) {
        List<AiFeedback> feedbacks = aiFeedbackRepository.findByUserId(userId);
        List<BudgetAlert> alerts = budgetAlertRepository.findByUserId(userId);

        Map<UUID, String> categoryNames = resolveCategoryNames(alerts);

        return Stream.concat(
                        feedbacks.stream().map(f -> (Notification) new AiFeedbackNotification(f)),
                        alerts.stream().map(a -> (Notification) new BudgetAlertNotification(
                                a, categoryNames.getOrDefault(a.getBudgetId(), "Unknown"))))
                .sorted(Comparator.comparing(Notification::createdAt).reversed())
                .toList();
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
