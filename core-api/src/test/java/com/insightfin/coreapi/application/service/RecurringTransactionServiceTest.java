package com.insightfin.coreapi.application.service;

import com.insightfin.coreapi.domain.event.TransactionCreatedEvent;
import com.insightfin.coreapi.domain.exception.DomainException;
import com.insightfin.coreapi.domain.exception.ResourceNotFoundException;
import com.insightfin.coreapi.domain.model.Category;
import com.insightfin.coreapi.domain.model.RecurrenceFrequency;
import com.insightfin.coreapi.domain.model.RecurringTransaction;
import com.insightfin.coreapi.domain.model.Transaction;
import com.insightfin.coreapi.domain.model.TransactionType;
import com.insightfin.coreapi.domain.port.out.CategoryRepository;
import com.insightfin.coreapi.domain.port.out.EventPublisher;
import com.insightfin.coreapi.domain.port.out.RecurringTransactionRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringTransactionServiceTest {

    @Mock RecurringTransactionRepository recurringRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock EventPublisher eventPublisher;

    private RecurringTransactionService service;

    private UUID userId;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        service = new RecurringTransactionService(
                recurringRepository, transactionRepository, categoryRepository, eventPublisher);
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
    }

    private void stubCategoryOwned() {
        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(buildCategory(categoryId, userId)));
    }

    // --- R1: create with startDate=today generates immediately and advances nextOccurrence ---
    @Test
    void create_withStartDateToday_generatesImmediatelyAndAdvances() {
        LocalDate today = LocalDate.now();
        stubCategoryOwned();
        when(recurringRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RecurringTransaction result = service.create(
                userId, categoryId, TransactionType.EXPENSE,
                new BigDecimal("100"), "Rent",
                RecurrenceFrequency.MONTHLY,
                today, null);

        verify(transactionRepository).save(any(Transaction.class));
        verify(eventPublisher).publishTransactionCreated(any(TransactionCreatedEvent.class));
        assertThat(result.getLastGeneratedAt()).isEqualTo(today);
        assertThat(result.getNextOccurrence()).isEqualTo(today.plusMonths(1));
    }

    // --- R2: create with future startDate does NOT generate immediately ---
    @Test
    void create_withFutureStartDate_doesNotGenerate() {
        LocalDate future = LocalDate.now().plusDays(7);
        stubCategoryOwned();
        when(recurringRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RecurringTransaction result = service.create(
                userId, categoryId, TransactionType.EXPENSE,
                new BigDecimal("100"), null,
                RecurrenceFrequency.WEEKLY,
                future, null);

        verify(transactionRepository, never()).save(any());
        verify(eventPublisher, never()).publishTransactionCreated(any());
        assertThat(result.getNextOccurrence()).isEqualTo(future);
        assertThat(result.getLastGeneratedAt()).isNull();
    }

    // --- R3: create rejects startDate in the past ---
    @Test
    void create_withPastStartDate_throws() {
        LocalDate past = LocalDate.now().minusDays(1);

        assertThatThrownBy(() -> service.create(
                userId, categoryId, TransactionType.EXPENSE,
                new BigDecimal("50"), null,
                RecurrenceFrequency.DAILY, past, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("past");

        verify(recurringRepository, never()).save(any());
    }

    // --- R4: create rejects endDate before startDate ---
    @Test
    void create_withEndDateBeforeStart_throws() {
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(2);

        assertThatThrownBy(() -> service.create(
                userId, categoryId, TransactionType.EXPENSE,
                new BigDecimal("50"), null,
                RecurrenceFrequency.WEEKLY, start, end))
                .isInstanceOf(DomainException.class);
    }

    // --- R5: BUG REGRESSION — update changing startDate without prior generation preserves first occurrence ---
    @Test
    void update_withStartChangedAndNoGenerationYet_preservesFirstOccurrence() {
        UUID recurringId = UUID.randomUUID();
        LocalDate oldStart = LocalDate.now().plusDays(5);
        LocalDate newStart = LocalDate.now().plusDays(10);
        RecurringTransaction existing = buildRecurring(recurringId, userId, categoryId,
                RecurrenceFrequency.MONTHLY, oldStart);
        existing.setLastGeneratedAt(null);
        existing.setNextOccurrence(oldStart);

        stubCategoryOwned();
        when(recurringRepository.findById(recurringId)).thenReturn(Optional.of(existing));
        when(recurringRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RecurringTransaction result = service.update(
                userId, recurringId, categoryId, TransactionType.EXPENSE,
                new BigDecimal("200"), "Rent",
                RecurrenceFrequency.MONTHLY,
                newStart, null);

        assertThat(result.getNextOccurrence())
                .as("first occurrence must remain at the new startDate, not be skipped")
                .isEqualTo(newStart);
    }

    // --- R6: update with lastGeneratedAt set uses it as anchor (not startDate) ---
    @Test
    void update_withStartChangedAfterGeneration_advancesFromLastGenerated() {
        UUID recurringId = UUID.randomUUID();
        LocalDate oldStart = LocalDate.now().minusMonths(3);
        LocalDate lastGen = LocalDate.now().minusMonths(1);
        LocalDate newStart = LocalDate.now();
        RecurringTransaction existing = buildRecurring(recurringId, userId, categoryId,
                RecurrenceFrequency.MONTHLY, oldStart);
        existing.setLastGeneratedAt(lastGen);
        existing.setNextOccurrence(lastGen.plusMonths(1));

        stubCategoryOwned();
        when(recurringRepository.findById(recurringId)).thenReturn(Optional.of(existing));
        when(recurringRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RecurringTransaction result = service.update(
                userId, recurringId, categoryId, TransactionType.EXPENSE,
                new BigDecimal("100"), null,
                RecurrenceFrequency.MONTHLY,
                newStart, null);

        assertThat(result.getNextOccurrence())
                .as("anchor must be lastGeneratedAt, not the new startDate")
                .isEqualTo(lastGen.plusMonths(1));
    }

    // --- R7: resume after long pause advances nextOccurrence past the gap ---
    @Test
    void resume_afterLongPause_advancesNextOccurrenceToFuture() {
        UUID recurringId = UUID.randomUUID();
        LocalDate today = LocalDate.now();
        LocalDate lastGen = today.minusMonths(3);
        RecurringTransaction existing = buildRecurring(recurringId, userId, categoryId,
                RecurrenceFrequency.MONTHLY, lastGen);
        existing.setLastGeneratedAt(lastGen);
        existing.setNextOccurrence(lastGen.plusMonths(1));
        existing.setPaused(true);

        when(recurringRepository.findById(recurringId)).thenReturn(Optional.of(existing));
        when(recurringRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RecurringTransaction result = service.resume(userId, recurringId);

        assertThat(result.isPaused()).isFalse();
        assertThat(result.getNextOccurrence())
                .as("must skip the paused-period occurrences and land on first future date")
                .isAfterOrEqualTo(today);
    }

    // --- R8: scheduler generates multiple missed occurrences ---
    @Test
    void generateDue_withMultipleMissedOccurrences_createsAll() {
        UUID recurringId = UUID.randomUUID();
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(3);
        RecurringTransaction existing = buildRecurring(recurringId, userId, categoryId,
                RecurrenceFrequency.DAILY, startDate);
        existing.setLastGeneratedAt(null);
        existing.setNextOccurrence(startDate);

        when(recurringRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.generateDue(existing, today);

        // 4 occurrences: D-3, D-2, D-1, D-0 (today)
        verify(transactionRepository, times(4)).save(any(Transaction.class));
        verify(eventPublisher, times(4)).publishTransactionCreated(any());

        ArgumentCaptor<RecurringTransaction> captor = ArgumentCaptor.forClass(RecurringTransaction.class);
        verify(recurringRepository, atLeastOnce()).save(captor.capture());
        RecurringTransaction saved = captor.getValue();
        assertThat(saved.getLastGeneratedAt()).isEqualTo(today);
        assertThat(saved.getNextOccurrence()).isEqualTo(today.plusDays(1));
    }

    // --- R9: delete removes the recurring (verified path + ownership) ---
    @Test
    void delete_removesRecurring() {
        UUID recurringId = UUID.randomUUID();
        RecurringTransaction existing = buildRecurring(recurringId, userId, categoryId,
                RecurrenceFrequency.MONTHLY, LocalDate.now().plusDays(1));
        when(recurringRepository.findById(recurringId)).thenReturn(Optional.of(existing));

        service.delete(userId, recurringId);

        verify(recurringRepository).deleteById(recurringId);
    }

    @Test
    void delete_byOtherUser_throws() {
        UUID recurringId = UUID.randomUUID();
        UUID otherUser = UUID.randomUUID();
        RecurringTransaction existing = buildRecurring(recurringId, otherUser, categoryId,
                RecurrenceFrequency.MONTHLY, LocalDate.now().plusDays(1));
        when(recurringRepository.findById(recurringId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.delete(userId, recurringId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(recurringRepository, never()).deleteById(any());
    }

    // --- fixtures ---

    private Category buildCategory(UUID id, UUID ownerId) {
        return new Category(id, ownerId, "Rent", TransactionType.EXPENSE, "🏠", "#000000", LocalDateTime.now());
    }

    private RecurringTransaction buildRecurring(UUID id, UUID ownerId, UUID catId,
                                                RecurrenceFrequency freq, LocalDate start) {
        RecurringTransaction r = new RecurringTransaction();
        r.setId(id);
        r.setUserId(ownerId);
        r.setCategoryId(catId);
        r.setType(TransactionType.EXPENSE);
        r.setAmount(new BigDecimal("100"));
        r.setFrequency(freq);
        r.setStartDate(start);
        r.setNextOccurrence(start);
        r.setPaused(false);
        r.setCreatedAt(LocalDateTime.now());
        r.setUpdatedAt(LocalDateTime.now());
        return r;
    }
}
