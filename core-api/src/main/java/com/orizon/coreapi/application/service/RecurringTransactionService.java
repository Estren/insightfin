package com.orizon.coreapi.application.service;

import com.orizon.coreapi.domain.event.TransactionCreatedEvent;
import com.orizon.coreapi.domain.exception.DomainException;
import com.orizon.coreapi.domain.exception.ResourceNotFoundException;
import com.orizon.coreapi.domain.model.Category;
import com.orizon.coreapi.domain.model.RecurrenceFrequency;
import com.orizon.coreapi.domain.model.RecurringTransaction;
import com.orizon.coreapi.domain.model.Transaction;
import com.orizon.coreapi.domain.model.TransactionType;
import com.orizon.coreapi.domain.port.in.CreateRecurringTransactionUseCase;
import com.orizon.coreapi.domain.port.in.DeleteRecurringTransactionUseCase;
import com.orizon.coreapi.domain.port.in.GetRecurringTransactionUseCase;
import com.orizon.coreapi.domain.port.in.ListRecurringTransactionsUseCase;
import com.orizon.coreapi.domain.port.in.PauseRecurringTransactionUseCase;
import com.orizon.coreapi.domain.port.in.UpdateRecurringTransactionUseCase;
import com.orizon.coreapi.domain.port.out.CategoryRepository;
import com.orizon.coreapi.domain.port.out.EventPublisher;
import com.orizon.coreapi.domain.port.out.RecurringTransactionRepository;
import com.orizon.coreapi.domain.port.out.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class RecurringTransactionService implements
        CreateRecurringTransactionUseCase,
        UpdateRecurringTransactionUseCase,
        DeleteRecurringTransactionUseCase,
        ListRecurringTransactionsUseCase,
        GetRecurringTransactionUseCase,
        PauseRecurringTransactionUseCase {

    private final RecurringTransactionRepository recurringRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final EventPublisher eventPublisher;

    public RecurringTransactionService(RecurringTransactionRepository recurringRepository,
                                       TransactionRepository transactionRepository,
                                       CategoryRepository categoryRepository,
                                       EventPublisher eventPublisher) {
        this.recurringRepository = recurringRepository;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public RecurringTransaction create(UUID userId, UUID categoryId, TransactionType type,
                                       BigDecimal amount, String description,
                                       RecurrenceFrequency frequency,
                                       LocalDate startDate, LocalDate endDate) {
        validateDates(startDate, endDate);
        ensureCategoryBelongsToUser(userId, categoryId);

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        RecurringTransaction recurring = new RecurringTransaction();
        recurring.setId(UUID.randomUUID());
        recurring.setUserId(userId);
        recurring.setCategoryId(categoryId);
        recurring.setType(type);
        recurring.setAmount(amount);
        recurring.setDescription(description);
        recurring.setFrequency(frequency);
        recurring.setStartDate(startDate);
        recurring.setEndDate(endDate);
        recurring.setNextOccurrence(startDate);
        recurring.setPaused(false);
        recurring.setCreatedAt(now);
        recurring.setUpdatedAt(now);

        RecurringTransaction saved = recurringRepository.save(recurring);

        if (startDate.equals(today)) {
            generateTransaction(saved, today);
            saved.setLastGeneratedAt(today);
            saved.setNextOccurrence(frequency.next(today));
            saved.setUpdatedAt(LocalDateTime.now());
            saved = recurringRepository.save(saved);
        }

        return saved;
    }

    @Override
    public RecurringTransaction update(UUID userId, UUID recurringId, UUID categoryId, TransactionType type,
                                       BigDecimal amount, String description,
                                       RecurrenceFrequency frequency,
                                       LocalDate startDate, LocalDate endDate) {
        RecurringTransaction recurring = findOwned(userId, recurringId);
        validateDates(startDate, endDate);
        ensureCategoryBelongsToUser(userId, categoryId);

        boolean frequencyChanged = recurring.getFrequency() != frequency;
        boolean startChanged = !recurring.getStartDate().equals(startDate);

        recurring.setCategoryId(categoryId);
        recurring.setType(type);
        recurring.setAmount(amount);
        recurring.setDescription(description);
        recurring.setFrequency(frequency);
        recurring.setStartDate(startDate);
        recurring.setEndDate(endDate);

        if (frequencyChanged || startChanged) {
            if (recurring.getLastGeneratedAt() == null) {
                recurring.setNextOccurrence(startDate);
            } else {
                recurring.setNextOccurrence(frequency.next(recurring.getLastGeneratedAt()));
            }
        }
        recurring.setUpdatedAt(LocalDateTime.now());

        return recurringRepository.save(recurring);
    }

    @Override
    public void delete(UUID userId, UUID recurringId) {
        findOwned(userId, recurringId);
        recurringRepository.deleteById(recurringId);
    }

    @Override
    public List<RecurringTransaction> list(UUID userId) {
        return recurringRepository.findByUserId(userId);
    }

    @Override
    public RecurringTransaction getById(UUID userId, UUID recurringId) {
        return findOwned(userId, recurringId);
    }

    @Override
    public RecurringTransaction pause(UUID userId, UUID recurringId) {
        RecurringTransaction recurring = findOwned(userId, recurringId);
        recurring.setPaused(true);
        recurring.setUpdatedAt(LocalDateTime.now());
        return recurringRepository.save(recurring);
    }

    @Override
    public RecurringTransaction resume(UUID userId, UUID recurringId) {
        RecurringTransaction recurring = findOwned(userId, recurringId);
        if (recurring.isPaused()) {
            LocalDate today = LocalDate.now();
            if (recurring.getNextOccurrence().isBefore(today)) {
                LocalDate anchor = recurring.getLastGeneratedAt() != null ? recurring.getLastGeneratedAt() : recurring.getStartDate();
                LocalDate next = recurring.getFrequency().next(anchor);
                while (next.isBefore(today)) {
                    next = recurring.getFrequency().next(next);
                }
                recurring.setNextOccurrence(next);
            }
        }
        recurring.setPaused(false);
        recurring.setUpdatedAt(LocalDateTime.now());
        return recurringRepository.save(recurring);
    }

    /**
     * Called by the scheduler. Catches up missed occurrences from {@code nextOccurrence} up to today,
     * creating one transaction per occurrence and advancing the cursor each step.
     */
    public void generateDue(RecurringTransaction recurring, LocalDate today) {
        LocalDate occurrence = recurring.getNextOccurrence();
        LocalDate endDate = recurring.getEndDate();

        while (!occurrence.isAfter(today) && (endDate == null || !occurrence.isAfter(endDate))) {
            generateTransaction(recurring, occurrence);
            recurring.setLastGeneratedAt(occurrence);
            occurrence = recurring.getFrequency().next(occurrence);
        }

        recurring.setNextOccurrence(occurrence);
        recurring.setUpdatedAt(LocalDateTime.now());
        recurringRepository.save(recurring);
    }

    private void generateTransaction(RecurringTransaction recurring, LocalDate date) {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setUserId(recurring.getUserId());
        transaction.setCategoryId(recurring.getCategoryId());
        transaction.setType(recurring.getType());
        transaction.setAmount(recurring.getAmount());
        transaction.setDescription(recurring.getDescription());
        transaction.setDate(date);
        transaction.setRecurringTransactionId(recurring.getId());
        transaction.setCreatedAt(LocalDateTime.now());

        Transaction saved = transactionRepository.save(transaction);

        eventPublisher.publishTransactionCreated(new TransactionCreatedEvent(
                saved.getUserId(), saved.getId(), saved.getCategoryId(),
                saved.getAmount(), saved.getType().name(), saved.getDate()));
    }

    private RecurringTransaction findOwned(UUID userId, UUID recurringId) {
        RecurringTransaction recurring = recurringRepository.findById(recurringId)
                .orElseThrow(() -> new ResourceNotFoundException("RecurringTransaction", recurringId));
        if (!recurring.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("RecurringTransaction", recurringId);
        }
        return recurring;
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        if (startDate.isBefore(today)) {
            throw new DomainException("Start date cannot be in the past");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new DomainException("End date must be on or after start date");
        }
    }

    private void ensureCategoryBelongsToUser(UUID userId, UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
        if (!category.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Category", categoryId);
        }
    }
}
