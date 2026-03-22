package com.orizon.coreapi.application.service;

import com.orizon.coreapi.domain.exception.ResourceNotFoundException;
import com.orizon.coreapi.domain.model.Transaction;
import com.orizon.coreapi.domain.model.TransactionType;
import com.orizon.coreapi.domain.port.in.CreateTransactionUseCase;
import com.orizon.coreapi.domain.port.in.ListTransactionsUseCase;
import com.orizon.coreapi.domain.port.out.CategoryRepository;
import com.orizon.coreapi.domain.port.out.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TransactionService implements CreateTransactionUseCase, ListTransactionsUseCase {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
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

        return transactionRepository.save(transaction);
    }

    @Override
    public List<Transaction> execute(UUID userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }
}
