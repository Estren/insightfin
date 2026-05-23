package com.orizon.coreapi.application.service;

import com.orizon.coreapi.domain.model.AiFeedback;
import com.orizon.coreapi.domain.model.AiFeedbackNotification;
import com.orizon.coreapi.domain.model.AiFeedbackType;
import com.orizon.coreapi.domain.model.Budget;
import com.orizon.coreapi.domain.model.BudgetAlert;
import com.orizon.coreapi.domain.model.BudgetAlertNotification;
import com.orizon.coreapi.domain.model.Category;
import com.orizon.coreapi.domain.model.Notification;
import com.orizon.coreapi.domain.model.NotificationKind;
import com.orizon.coreapi.domain.model.TransactionType;
import com.orizon.coreapi.domain.model.UnreadCounts;
import com.orizon.coreapi.domain.port.out.AiFeedbackRepository;
import com.orizon.coreapi.domain.port.out.BudgetAlertRepository;
import com.orizon.coreapi.domain.port.out.BudgetRepository;
import com.orizon.coreapi.domain.port.out.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock AiFeedbackRepository aiFeedbackRepository;
    @Mock BudgetAlertRepository budgetAlertRepository;
    @Mock BudgetRepository budgetRepository;
    @Mock CategoryRepository categoryRepository;

    private NotificationService service;
    private UUID userId;

    @BeforeEach
    void setUp() {
        service = new NotificationService(aiFeedbackRepository, budgetAlertRepository,
                budgetRepository, categoryRepository);
        userId = UUID.randomUUID();
    }

    // --- N1: empty when both sources are empty ---
    @Test
    void list_bothSourcesEmpty_returnsEmpty() {
        when(aiFeedbackRepository.findByUserId(userId)).thenReturn(List.of());
        when(budgetAlertRepository.findByUserId(userId)).thenReturn(List.of());

        assertThat(service.list(userId)).isEmpty();
    }

    // --- N2: combines feedbacks + alerts, sorted by createdAt desc ---
    @Test
    void list_mixedSources_returnsSortedDescendingByCreatedAt() {
        LocalDateTime t0 = LocalDateTime.of(2026, 5, 20, 10, 0);
        LocalDateTime t1 = LocalDateTime.of(2026, 5, 21, 10, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 5, 22, 10, 0);

        AiFeedback feedback = buildFeedback(t1);
        BudgetAlert older = buildAlert(t0);
        BudgetAlert newer = buildAlert(t2);

        when(aiFeedbackRepository.findByUserId(userId)).thenReturn(List.of(feedback));
        when(budgetAlertRepository.findByUserId(userId)).thenReturn(List.of(older, newer));
        givenBudgetWithCategory(older.getBudgetId(), "Food");
        givenBudgetWithCategory(newer.getBudgetId(), "Food");

        List<Notification> result = service.list(userId);

        assertThat(result).extracting(Notification::createdAt).containsExactly(t2, t1, t0);
        assertThat(result).extracting(Notification::kind).containsExactly(
                NotificationKind.BUDGET_ALERT, NotificationKind.AI_FEEDBACK, NotificationKind.BUDGET_ALERT);
    }

    // --- N3: enriches budget alert with the category name ---
    @Test
    void list_budgetAlert_includesCategoryName() {
        BudgetAlert alert = buildAlert(LocalDateTime.now());
        when(aiFeedbackRepository.findByUserId(userId)).thenReturn(List.of());
        when(budgetAlertRepository.findByUserId(userId)).thenReturn(List.of(alert));
        givenBudgetWithCategory(alert.getBudgetId(), "Transport");

        List<Notification> result = service.list(userId);

        assertThat(result).hasSize(1);
        BudgetAlertNotification n = (BudgetAlertNotification) result.get(0);
        assertThat(n.categoryName()).isEqualTo("Transport");
    }

    // --- N4: missing budget → "Unknown" fallback (alert outlives the budget) ---
    @Test
    void list_budgetMissing_fallsBackToUnknown() {
        BudgetAlert alert = buildAlert(LocalDateTime.now());
        when(aiFeedbackRepository.findByUserId(userId)).thenReturn(List.of());
        when(budgetAlertRepository.findByUserId(userId)).thenReturn(List.of(alert));
        when(budgetRepository.findById(alert.getBudgetId())).thenReturn(Optional.empty());

        List<Notification> result = service.list(userId);

        BudgetAlertNotification n = (BudgetAlertNotification) result.get(0);
        assertThat(n.categoryName()).isEqualTo("Unknown");
    }

    // --- N5: distinct budgets fetched once (no N+1 over alerts) ---
    @Test
    void list_repeatedBudgetAcrossAlerts_isFetchedOnce() {
        UUID sharedBudgetId = UUID.randomUUID();
        BudgetAlert at50 = buildAlertForBudget(sharedBudgetId, 50, LocalDateTime.now().minusHours(2));
        BudgetAlert at80 = buildAlertForBudget(sharedBudgetId, 80, LocalDateTime.now().minusHours(1));
        BudgetAlert at100 = buildAlertForBudget(sharedBudgetId, 100, LocalDateTime.now());

        when(aiFeedbackRepository.findByUserId(userId)).thenReturn(List.of());
        when(budgetAlertRepository.findByUserId(userId)).thenReturn(List.of(at50, at80, at100));
        givenBudgetWithCategory(sharedBudgetId, "Food");

        service.list(userId);

        verify(budgetRepository, times(1)).findById(sharedBudgetId);
    }

    // --- N6: unread counts sum the two repos ---
    @Test
    void getUnreadCounts_addsBothSources() {
        when(aiFeedbackRepository.countUnreadByUserId(userId)).thenReturn(3);
        when(budgetAlertRepository.countUnreadByUserId(userId)).thenReturn(2);

        UnreadCounts result = service.count(userId);

        assertThat(result.aiFeedbacks()).isEqualTo(3);
        assertThat(result.budgetAlerts()).isEqualTo(2);
        assertThat(result.total()).isEqualTo(5);
    }

    // ---------- helpers ----------

    private AiFeedback buildFeedback(LocalDateTime createdAt) {
        return new AiFeedback(UUID.randomUUID(), userId, AiFeedbackType.MONTHLY_REPORT,
                "Monthly report", "...", null, "2026-05", false, createdAt);
    }

    private BudgetAlert buildAlert(LocalDateTime createdAt) {
        return buildAlertForBudget(UUID.randomUUID(), 80, createdAt);
    }

    private BudgetAlert buildAlertForBudget(UUID budgetId, int threshold, LocalDateTime createdAt) {
        return new BudgetAlert(UUID.randomUUID(), userId, budgetId, threshold,
                new BigDecimal("80.00"), new BigDecimal("100.00"),
                createdAt, false, createdAt);
    }

    private void givenBudgetWithCategory(UUID budgetId, String categoryName) {
        UUID categoryId = UUID.randomUUID();
        Budget budget = new Budget();
        budget.setId(budgetId);
        budget.setUserId(userId);
        budget.setCategoryId(categoryId);
        budget.setAmount(new BigDecimal("100.00"));
        budget.setMonth("2026-05");
        budget.setCreatedAt(LocalDateTime.now());
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));

        Category category = new Category();
        category.setId(categoryId);
        category.setUserId(userId);
        category.setName(categoryName);
        category.setType(TransactionType.EXPENSE);
        category.setCreatedAt(LocalDateTime.now());
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
    }
}
