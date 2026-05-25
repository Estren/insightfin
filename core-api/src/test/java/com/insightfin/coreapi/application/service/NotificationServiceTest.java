package com.insightfin.coreapi.application.service;

import com.insightfin.coreapi.application.pagination.Cursor;
import com.insightfin.coreapi.application.pagination.Page;
import com.insightfin.coreapi.application.pagination.PaginationParams;
import com.insightfin.coreapi.domain.model.AiFeedback;
import com.insightfin.coreapi.domain.model.AiFeedbackType;
import com.insightfin.coreapi.domain.model.Budget;
import com.insightfin.coreapi.domain.model.BudgetAlert;
import com.insightfin.coreapi.domain.model.BudgetAlertNotification;
import com.insightfin.coreapi.domain.model.Category;
import com.insightfin.coreapi.domain.model.Notification;
import com.insightfin.coreapi.domain.model.NotificationKind;
import com.insightfin.coreapi.domain.model.TransactionType;
import com.insightfin.coreapi.domain.model.UnreadCounts;
import com.insightfin.coreapi.domain.port.out.AiFeedbackRepository;
import com.insightfin.coreapi.domain.port.out.BudgetAlertRepository;
import com.insightfin.coreapi.domain.port.out.BudgetRepository;
import com.insightfin.coreapi.domain.port.out.CategoryRepository;
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
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
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
    void list_bothSourcesEmpty_returnsEmptyPage() {
        when(aiFeedbackRepository.findPage(eq(userId), any(), any(), anyInt())).thenReturn(List.of());
        when(budgetAlertRepository.findPage(eq(userId), any(), any(), anyInt())).thenReturn(List.of());

        Page<Notification> page = service.list(userId, firstPage(20));

        assertThat(page.items()).isEmpty();
        assertThat(page.hasMore()).isFalse();
        assertThat(page.nextCursor()).isNull();
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

        when(aiFeedbackRepository.findPage(eq(userId), any(), any(), anyInt()))
                .thenReturn(List.of(feedback));
        when(budgetAlertRepository.findPage(eq(userId), any(), any(), anyInt()))
                .thenReturn(List.of(newer, older));
        givenBudgetWithCategory(older.getBudgetId(), "Food");
        givenBudgetWithCategory(newer.getBudgetId(), "Food");

        Page<Notification> page = service.list(userId, firstPage(20));

        assertThat(page.items()).extracting(Notification::createdAt).containsExactly(t2, t1, t0);
        assertThat(page.items()).extracting(Notification::kind).containsExactly(
                NotificationKind.BUDGET_ALERT, NotificationKind.AI_FEEDBACK, NotificationKind.BUDGET_ALERT);
    }

    // --- N3: enriches budget alert with the category name ---
    @Test
    void list_budgetAlert_includesCategoryName() {
        BudgetAlert alert = buildAlert(LocalDateTime.now());
        when(aiFeedbackRepository.findPage(eq(userId), any(), any(), anyInt())).thenReturn(List.of());
        when(budgetAlertRepository.findPage(eq(userId), any(), any(), anyInt())).thenReturn(List.of(alert));
        givenBudgetWithCategory(alert.getBudgetId(), "Transport");

        Page<Notification> page = service.list(userId, firstPage(20));

        assertThat(page.items()).hasSize(1);
        BudgetAlertNotification n = (BudgetAlertNotification) page.items().get(0);
        assertThat(n.categoryName()).isEqualTo("Transport");
    }

    // --- N4: missing budget → "Unknown" fallback ---
    @Test
    void list_budgetMissing_fallsBackToUnknown() {
        BudgetAlert alert = buildAlert(LocalDateTime.now());
        when(aiFeedbackRepository.findPage(eq(userId), any(), any(), anyInt())).thenReturn(List.of());
        when(budgetAlertRepository.findPage(eq(userId), any(), any(), anyInt())).thenReturn(List.of(alert));
        when(budgetRepository.findById(alert.getBudgetId())).thenReturn(Optional.empty());

        Page<Notification> page = service.list(userId, firstPage(20));

        BudgetAlertNotification n = (BudgetAlertNotification) page.items().get(0);
        assertThat(n.categoryName()).isEqualTo("Unknown");
    }

    // --- N5: distinct budgets fetched once (no N+1 over alerts) ---
    @Test
    void list_repeatedBudgetAcrossAlerts_isFetchedOnce() {
        UUID sharedBudgetId = UUID.randomUUID();
        BudgetAlert at100 = buildAlertForBudget(sharedBudgetId, 100, LocalDateTime.now());
        BudgetAlert at80 = buildAlertForBudget(sharedBudgetId, 80, LocalDateTime.now().minusHours(1));
        BudgetAlert at50 = buildAlertForBudget(sharedBudgetId, 50, LocalDateTime.now().minusHours(2));

        when(aiFeedbackRepository.findPage(eq(userId), any(), any(), anyInt())).thenReturn(List.of());
        when(budgetAlertRepository.findPage(eq(userId), any(), any(), anyInt()))
                .thenReturn(List.of(at100, at80, at50));
        givenBudgetWithCategory(sharedBudgetId, "Food");

        service.list(userId, firstPage(20));

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

    // --- N7: cursor in params flows to both repos as the same (createdAt, id) tuple ---
    @Test
    void list_cursorParam_isPassedToBothRepos() {
        LocalDateTime cursorAt = LocalDateTime.of(2026, 5, 23, 14, 0);
        UUID cursorId = UUID.randomUUID();
        Cursor cursor = new Cursor(cursorAt, cursorId);

        when(aiFeedbackRepository.findPage(userId, cursorAt, cursorId, 21)).thenReturn(List.of());
        when(budgetAlertRepository.findPage(userId, cursorAt, cursorId, 21)).thenReturn(List.of());

        service.list(userId, new PaginationParams(20, cursor));

        verify(aiFeedbackRepository).findPage(userId, cursorAt, cursorId, 21);
        verify(budgetAlertRepository).findPage(userId, cursorAt, cursorId, 21);
    }

    // --- N8: first page with no cursor passes (null, null) to repos with limit+1 ---
    @Test
    void list_firstPage_callsReposWithNullCursorAndLimitPlusOne() {
        when(aiFeedbackRepository.findPage(eq(userId), any(), any(), anyInt())).thenReturn(List.of());
        when(budgetAlertRepository.findPage(eq(userId), any(), any(), anyInt())).thenReturn(List.of());

        service.list(userId, firstPage(20));

        verify(aiFeedbackRepository).findPage(userId, null, null, 21);
        verify(budgetAlertRepository).findPage(userId, null, null, 21);
    }

    // --- N9: more results than limit → hasMore=true + nextCursor pointing at last returned item ---
    @Test
    void list_resultsExceedLimit_marksHasMoreAndCursorsLastReturned() {
        // 25 feedbacks with descending timestamps → only top 21 reach the merge,
        // top 20 are returned, cursor points to the 20th.
        List<AiFeedback> feedbacks = IntStream.range(0, 25)
                .mapToObj(i -> buildFeedback(LocalDateTime.of(2026, 5, 23, 12, 0).minusMinutes(i)))
                .toList();
        when(aiFeedbackRepository.findPage(eq(userId), any(), any(), anyInt()))
                .thenReturn(feedbacks.subList(0, 21));  // repo respects the limit+1
        when(budgetAlertRepository.findPage(eq(userId), any(), any(), anyInt())).thenReturn(List.of());

        Page<Notification> page = service.list(userId, firstPage(20));

        assertThat(page.items()).hasSize(20);
        assertThat(page.hasMore()).isTrue();
        assertThat(page.nextCursor()).isNotNull();
        Cursor decoded = Cursor.decode(page.nextCursor());
        assertThat(decoded.createdAt()).isEqualTo(feedbacks.get(19).getCreatedAt());
        assertThat(decoded.id()).isEqualTo(feedbacks.get(19).getId());
    }

    // ---------- helpers ----------

    private static PaginationParams firstPage(int limit) {
        return new PaginationParams(limit, null);
    }

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
