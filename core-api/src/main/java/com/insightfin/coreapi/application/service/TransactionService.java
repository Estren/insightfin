package com.insightfin.coreapi.application.service;

import com.insightfin.coreapi.domain.event.TransactionCreatedEvent;
import com.insightfin.coreapi.domain.exception.ResourceNotFoundException;
import com.insightfin.coreapi.domain.model.Transaction;
import com.insightfin.coreapi.domain.model.TransactionType;
import com.insightfin.coreapi.domain.port.in.CreateTransactionUseCase;
import com.insightfin.coreapi.domain.port.in.DeleteTransactionUseCase;
import com.insightfin.coreapi.domain.port.in.ListTransactionsUseCase;
import com.insightfin.coreapi.domain.port.in.UpdateTransactionUseCase;
import com.insightfin.coreapi.domain.port.out.CategoryRepository;
import com.insightfin.coreapi.domain.port.out.EventPublisher;
import com.insightfin.coreapi.domain.port.out.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TransactionService implements CreateTransactionUseCase, ListTransactionsUseCase,
        UpdateTransactionUseCase, DeleteTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final EventPublisher eventPublisher;

    public TransactionService(TransactionRepository transactionRepository,
                              CategoryRepository categoryRepository,
                              EventPublisher eventPublisher) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Transaction execute(UUID userId, UUID categoryId, TransactionType type,
                               BigDecimal amount, String description, LocalDate date) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setUserId(userId);
        transaction.setCategoryId(categoryId);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setDate(date);
        transaction.setCreatedAt(LocalDateTime.now());

        Transaction saved = transactionRepository.save(transaction);

        eventPublisher.publishTransactionCreated(new TransactionCreatedEvent(
                saved.getUserId(), saved.getId(), saved.getCategoryId(),
                saved.getAmount(), saved.getType().name(), saved.getDate()));

        return saved;
    }

    @Override
    public List<Transaction> execute(UUID userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }

    @Override
    public Transaction execute(UUID userId, UUID transactionId, UUID categoryId, TransactionType type,
                               BigDecimal amount, String description, LocalDate date) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", transactionId));

        if (!transaction.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Transaction", transactionId);
        }

        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));

        transaction.setCategoryId(categoryId);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setDate(date);

        return transactionRepository.save(transaction);
    }

    @Override
    public void execute(UUID userId, UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", transactionId));

        if (!transaction.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Transaction", transactionId);
        }

        transactionRepository.deleteById(transactionId);
    }
}
