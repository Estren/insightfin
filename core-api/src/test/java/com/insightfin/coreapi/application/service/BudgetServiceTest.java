package com.insightfin.coreapi.application.service;

import com.insightfin.coreapi.domain.exception.DuplicateResourceException;
import com.insightfin.coreapi.domain.exception.ResourceNotFoundException;
import com.insightfin.coreapi.domain.model.Budget;
import com.insightfin.coreapi.domain.model.BudgetStatus;
import com.insightfin.coreapi.domain.model.Category;
import com.insightfin.coreapi.domain.model.TransactionType;
import com.insightfin.coreapi.domain.port.out.BudgetRepository;
import com.insightfin.coreapi.domain.port.out.CategoryRepository;
import com.insightfin.coreapi.domain.port.out.TransactionRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock BudgetRepository budgetRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock TransactionRepository transactionRepository;

    private BudgetService service;

    @BeforeEach
    void setUp() {
        service = new BudgetService(budgetRepository, categoryRepository, transactionRepository);
    }

    // --- B1 ---
    @Test
    void create_succeeds_savesBudget() {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(buildCategory(categoryId)));
        when(budgetRepository.findByUserIdAndCategoryIdAndMonth(userId, categoryId, "2026-05"))
                .thenReturn(Optional.empty());

        Budget saved = buildBudget(UUID.randomUUID(), userId, categoryId, new BigDecimal("500.00"));
        when(budgetRepository.save(any())).thenReturn(saved);

        Budget result = service.execute(userId, categoryId, new BigDecimal("500.00"), "2026-05");

        assertThat(result).isEqualTo(saved);
        verify(budgetRepository).save(any(Budget.class));
    }

    // --- B2 ---
    @Test
    void create_throwsWhenCategoryNotFound() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.execute(UUID.randomUUID(), categoryId, new BigDecimal("300.00"), "2026-05"))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(budgetRepository);
    }

    // --- B3 ---
    @Test
    void create_throwsWhenDuplicateBudget() {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(buildCategory(categoryId)));
        when(budgetRepository.findByUserIdAndCategoryIdAndMonth(userId, categoryId, "2026-05"))
                .thenReturn(Optional.of(buildBudget(UUID.randomUUID(), userId, categoryId, new BigDecimal("200.00"))));

        assertThatThrownBy(() ->
                service.execute(userId, categoryId, new BigDecimal("300.00"), "2026-05"))
                .isInstanceOf(DuplicateResourceException.class);

        verify(budgetRepository, never()).save(any());
    }

    // --- B4 ---
    @Test
    void list_delegatesToRepository() {
        UUID userId = UUID.randomUUID();
        List<Budget> expected = List.of(
                buildBudget(UUID.randomUUID(), userId, UUID.randomUUID(), new BigDecimal("400.00")));

        when(budgetRepository.findByUserIdAndMonth(userId, "2026-05")).thenReturn(expected);

        List<Budget> result = service.execute(userId, "2026-05");

        assertThat(result).isEqualTo(expected);
        verify(budgetRepository).findByUserIdAndMonth(userId, "2026-05");
    }

    // --- B5 ---
    @Test
    void getStatus_calculatesPercentageCorrectly() {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();

        Budget budget = buildBudget(budgetId, userId, categoryId, new BigDecimal("200.00"));
        when(budgetRepository.findByUserIdAndMonth(userId, "2026-05")).thenReturn(List.of(budget));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(buildCategory(categoryId)));
        when(transactionRepository.sumAmountByUserIdAndCategoryIdAndMonth(userId, categoryId, "2026-05"))
                .thenReturn(new BigDecimal("50.00"));

        List<BudgetStatus> statuses = service.execute(userId, "2026-05", true);

        assertThat(statuses).hasSize(1);
        assertThat(statuses.get(0).getPercentageUsed()).isEqualByComparingTo("25.00");
        assertThat(statuses.get(0).getSpentAmount()).isEqualByComparingTo("50.00");
    }

    // --- B6 ---
    @Test
    void getStatus_returnsZeroPercentageWhenBudgetAmountIsZero() {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();

        Budget budget = buildBudget(budgetId, userId, categoryId, BigDecimal.ZERO);
        when(budgetRepository.findByUserIdAndMonth(userId, "2026-05")).thenReturn(List.of(budget));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(buildCategory(categoryId)));
        when(transactionRepository.sumAmountByUserIdAndCategoryIdAndMonth(userId, categoryId, "2026-05"))
                .thenReturn(new BigDecimal("30.00"));

        List<BudgetStatus> statuses = service.execute(userId, "2026-05", true);

        assertThat(statuses.get(0).getPercentageUsed()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // --- B7 ---
    @Test
    void update_throwsWhenOwnershipMismatch() {
        UUID budgetId = UUID.randomUUID();
        UUID realOwner = UUID.randomUUID();
        UUID attacker = UUID.randomUUID();

        Budget existing = buildBudget(budgetId, realOwner, UUID.randomUUID(), new BigDecimal("100.00"));
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() ->
                service.execute(attacker, budgetId, new BigDecimal("200.00")))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(budgetRepository, never()).save(any());
    }

    // --- B8 ---
    @Test
    void delete_throwsWhenOwnershipMismatch() {
        UUID budgetId = UUID.randomUUID();
        UUID realOwner = UUID.randomUUID();
        UUID attacker = UUID.randomUUID();

        Budget existing = buildBudget(budgetId, realOwner, UUID.randomUUID(), new BigDecimal("100.00"));
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.execute(attacker, budgetId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(budgetRepository, never()).deleteById(any());
    }

    // --- fixtures ---

    private Budget buildBudget(UUID id, UUID userId, UUID categoryId, BigDecimal amount) {
        Budget b = new Budget();
        b.setId(id);
        b.setUserId(userId);
        b.setCategoryId(categoryId);
        b.setAmount(amount);
        b.setMonth("2026-05");
        b.setCreatedAt(LocalDateTime.now());
        return b;
    }

    private Category buildCategory(UUID id) {
        Category c = new Category();
        c.setId(id);
        c.setName("Food");
        c.setType(TransactionType.EXPENSE);
        return c;
    }
}
