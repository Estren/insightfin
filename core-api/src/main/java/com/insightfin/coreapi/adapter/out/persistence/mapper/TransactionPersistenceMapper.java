package com.insightfin.coreapi.adapter.out.persistence.mapper;

import com.insightfin.coreapi.adapter.out.persistence.entity.TransactionEntity;
import com.insightfin.coreapi.domain.model.Transaction;
import com.insightfin.coreapi.domain.model.TransactionType;

public class TransactionPersistenceMapper {

    private TransactionPersistenceMapper() {}

    public static TransactionEntity toEntity(Transaction transaction) {
        TransactionEntity entity = new TransactionEntity();
        entity.setId(transaction.getId());
        entity.setUserId(transaction.getUserId());
        entity.setCategoryId(transaction.getCategoryId());
        entity.setType(transaction.getType().name());
        entity.setAmount(transaction.getAmount());
        entity.setDescription(transaction.getDescription());
        entity.setDate(transaction.getDate());
        entity.setRecurringTransactionId(transaction.getRecurringTransactionId());
        entity.setCreatedAt(transaction.getCreatedAt());
        return entity;
    }

    public static Transaction toDomain(TransactionEntity entity) {
        Transaction transaction = new Transaction(
                entity.getId(),
                entity.getUserId(),
                entity.getCategoryId(),
                TransactionType.valueOf(entity.getType()),
                entity.getAmount(),
                entity.getDescription(),
                entity.getDate(),
                entity.getCreatedAt()
        );
        transaction.setRecurringTransactionId(entity.getRecurringTransactionId());
        return transaction;
    }
}
