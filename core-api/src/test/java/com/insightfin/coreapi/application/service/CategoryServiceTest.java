package com.insightfin.coreapi.application.service;

import com.insightfin.coreapi.domain.exception.DomainException;
import com.insightfin.coreapi.domain.exception.ResourceNotFoundException;
import com.insightfin.coreapi.domain.model.Category;
import com.insightfin.coreapi.domain.model.TransactionType;
import com.insightfin.coreapi.domain.port.in.DeleteCategoryUseCase;
import com.insightfin.coreapi.domain.port.out.CategoryRepository;
import com.insightfin.coreapi.domain.port.out.RecurringTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock CategoryRepository categoryRepository;
    @Mock RecurringTransactionRepository recurringTransactionRepository;

    private CategoryService service;

    @BeforeEach
    void setUp() {
        service = new CategoryService(categoryRepository, recurringTransactionRepository);
    }

    // --- C1 ---
    @Test
    void create_succeeds_savesCategory() {
        UUID userId = UUID.randomUUID();
        Category saved = buildCategory(UUID.randomUUID(), userId, TransactionType.EXPENSE);
        when(categoryRepository.save(any())).thenReturn(saved);

        Category result = service.execute(userId, "Food", TransactionType.EXPENSE, "🍔", "#FF0000");

        assertThat(result).isEqualTo(saved);
        verify(categoryRepository).save(any(Category.class));
    }

    // --- C2 ---
    @Test
    void list_withType_filtersByType() {
        UUID userId = UUID.randomUUID();
        List<Category> expected = List.of(buildCategory(UUID.randomUUID(), userId, TransactionType.EXPENSE));
        when(categoryRepository.findByUserIdAndType(userId, TransactionType.EXPENSE)).thenReturn(expected);

        List<Category> result = service.execute(userId, TransactionType.EXPENSE);

        assertThat(result).isEqualTo(expected);
        verify(categoryRepository).findByUserIdAndType(userId, TransactionType.EXPENSE);
        verify(categoryRepository, never()).findByUserId(any());
    }

    // --- C3 ---
    @Test
    void list_withoutType_returnsAll() {
        UUID userId = UUID.randomUUID();
        List<Category> expected = List.of(
                buildCategory(UUID.randomUUID(), userId, TransactionType.EXPENSE),
                buildCategory(UUID.randomUUID(), userId, TransactionType.INCOME));
        when(categoryRepository.findByUserId(userId)).thenReturn(expected);

        List<Category> result = service.execute(userId, (TransactionType) null);

        assertThat(result).isEqualTo(expected);
        verify(categoryRepository).findByUserId(userId);
        verify(categoryRepository, never()).findByUserIdAndType(any(), any());
    }

    // --- C4 ---
    @Test
    void update_throwsWhenOwnershipMismatch() {
        UUID categoryId = UUID.randomUUID();
        UUID realOwner = UUID.randomUUID();
        UUID attacker = UUID.randomUUID();

        Category existing = buildCategory(categoryId, realOwner, TransactionType.EXPENSE);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() ->
                service.execute(attacker, categoryId, "New Name", TransactionType.INCOME, "🏦", "#000"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, never()).save(any());
    }

    // --- C5 ---
    @Test
    void delete_throwsWhenCategoryHasTransactions() {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Category existing = buildCategory(categoryId, userId, TransactionType.EXPENSE);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existing));
        when(categoryRepository.hasTransactions(categoryId)).thenReturn(true);

        DeleteCategoryUseCase deleteUseCase = service;
        assertThatThrownBy(() -> deleteUseCase.execute(userId, categoryId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Cannot delete category with existing transactions");

        verify(categoryRepository, never()).deleteById(any());
    }

    // --- C6 ---
    @Test
    void delete_throwsWhenOwnershipMismatch() {
        UUID categoryId = UUID.randomUUID();
        UUID realOwner = UUID.randomUUID();
        UUID attacker = UUID.randomUUID();

        Category existing = buildCategory(categoryId, realOwner, TransactionType.EXPENSE);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existing));

        DeleteCategoryUseCase deleteUseCase = service;
        assertThatThrownBy(() -> deleteUseCase.execute(attacker, categoryId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, never()).deleteById(any());
    }

    // --- fixtures ---

    private Category buildCategory(UUID id, UUID userId, TransactionType type) {
        Category c = new Category();
        c.setId(id);
        c.setUserId(userId);
        c.setName("Test Category");
        c.setType(type);
        c.setIcon("🏷️");
        c.setColor("#FFFFFF");
        c.setCreatedAt(LocalDateTime.now());
        return c;
    }
}
