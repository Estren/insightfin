package com.insightfin.coreapi.application.service;

import com.insightfin.coreapi.domain.event.TransactionCreatedEvent;
import com.insightfin.coreapi.domain.exception.ResourceNotFoundException;
import com.insightfin.coreapi.domain.model.Category;
import com.insightfin.coreapi.domain.model.Transaction;
import com.insightfin.coreapi.domain.model.TransactionType;
import com.insightfin.coreapi.domain.port.out.CategoryRepository;
import com.insightfin.coreapi.domain.port.out.EventPublisher;
import com.insightfin.coreapi.domain.port.out.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock TransactionRepository transactionRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock EventPublisher eventPublisher;

    private TransactionService service;

    @BeforeEach
    void setUp() {
        service = new TransactionService(transactionRepository, categoryRepository, eventPublisher);
    }

    // --- T1 ---
    @Test
    void create_succeeds_savesTransactionAndPublishesEvent() {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 5, 1);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(buildCategory(categoryId)));

        Transaction saved = buildTransaction(UUID.randomUUID(), userId, categoryId);
        when(transactionRepository.save(any())).thenReturn(saved);

        Transaction result = service.execute(userId, categoryId, TransactionType.EXPENSE,
                new BigDecimal("150.00"), "Groceries", date);

        assertThat(result).isEqualTo(saved);
        verify(transactionRepository).save(any(Transaction.class));

        ArgumentCaptor<TransactionCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(TransactionCreatedEvent.class);
        verify(eventPublisher).publishTransactionCreated(eventCaptor.capture());
        assertThat(eventCaptor.getValue().userId()).isEqualTo(saved.getUserId());
    }

    // --- T2 ---
    @Test
    void create_throwsWhenCategoryNotFound() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.execute(UUID.randomUUID(), categoryId, TransactionType.EXPENSE,
                        new BigDecimal("50.00"), "Test", LocalDate.now()))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(transactionRepository, eventPublisher);
    }

    // --- T3 ---
    @Test
    void list_delegatesToRepository() {
        UUID userId = UUID.randomUUID();
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 31);
        List<Transaction> expected = List.of(buildTransaction(UUID.randomUUID(), userId, UUID.randomUUID()));

        when(transactionRepository.findByUserIdAndDateBetween(userId, start, end)).thenReturn(expected);

        List<Transaction> result = service.execute(userId, start, end);

        assertThat(result).isEqualTo(expected);
        verify(transactionRepository).findByUserIdAndDateBetween(userId, start, end);
    }

    // --- T4 ---
    @Test
    void update_succeeds_updatesFieldsAndSaves() {
        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        UUID newCategoryId = UUID.randomUUID();

        Transaction existing = buildTransaction(transactionId, userId, UUID.randomUUID());
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(newCategoryId)).thenReturn(Optional.of(buildCategory(newCategoryId)));
        when(transactionRepository.save(existing)).thenReturn(existing);

        Transaction result = service.execute(userId, transactionId, newCategoryId,
                TransactionType.INCOME, new BigDecimal("200.00"), "Salary", LocalDate.now());

        assertThat(result.getCategoryId()).isEqualTo(newCategoryId);
        assertThat(result.getType()).isEqualTo(TransactionType.INCOME);
        assertThat(result.getAmount()).isEqualByComparingTo("200.00");
        verify(transactionRepository).save(existing);
    }

    // --- T5 ---
    @Test
    void update_throwsWhenTransactionNotFound() {
        UUID transactionId = UUID.randomUUID();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.execute(UUID.randomUUID(), transactionId, UUID.randomUUID(),
                        TransactionType.EXPENSE, new BigDecimal("10.00"), "X", LocalDate.now()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- T6 ---
    @Test
    void update_throwsWhenOwnershipMismatch() {
        UUID transactionId = UUID.randomUUID();
        UUID realOwner = UUID.randomUUID();
        UUID attacker = UUID.randomUUID();

        Transaction existing = buildTransaction(transactionId, realOwner, UUID.randomUUID());
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() ->
                service.execute(attacker, transactionId, UUID.randomUUID(),
                        TransactionType.EXPENSE, new BigDecimal("10.00"), "X", LocalDate.now()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(transactionRepository, never()).save(any());
    }

    // --- T7 ---
    @Test
    void delete_succeeds_callsDeleteById() {
        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        Transaction existing = buildTransaction(transactionId, userId, UUID.randomUUID());
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(existing));

        service.execute(userId, transactionId);

        verify(transactionRepository).deleteById(transactionId);
    }

    // --- T8 ---
    @Test
    void delete_throwsWhenOwnershipMismatch() {
        UUID transactionId = UUID.randomUUID();
        UUID realOwner = UUID.randomUUID();
        UUID attacker = UUID.randomUUID();

        Transaction existing = buildTransaction(transactionId, realOwner, UUID.randomUUID());
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.execute(attacker, transactionId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(transactionRepository, never()).deleteById(any());
    }

    // --- fixtures ---

    private Transaction buildTransaction(UUID id, UUID userId, UUID categoryId) {
        Transaction t = new Transaction();
        t.setId(id);
        t.setUserId(userId);
        t.setCategoryId(categoryId);
        t.setType(TransactionType.EXPENSE);
        t.setAmount(new BigDecimal("100.00"));
        t.setDescription("Test transaction");
        t.setDate(LocalDate.of(2026, 5, 1));
        t.setCreatedAt(LocalDateTime.now());
        return t;
    }

    private Category buildCategory(UUID id) {
        Category c = new Category();
        c.setId(id);
        c.setName("Food");
        c.setType(TransactionType.EXPENSE);
        return c;
    }
}
