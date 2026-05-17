package com.orizon.coreapi.adapter.out.persistence.mapper;

import com.orizon.coreapi.adapter.out.persistence.entity.RecurringTransactionEntity;
import com.orizon.coreapi.domain.model.RecurrenceFrequency;
import com.orizon.coreapi.domain.model.RecurringTransaction;
import com.orizon.coreapi.domain.model.TransactionType;

public class RecurringTransactionPersistenceMapper {

    private RecurringTransactionPersistenceMapper() {}

    public static RecurringTransactionEntity toEntity(RecurringTransaction model) {
        RecurringTransactionEntity entity = new RecurringTransactionEntity();
        entity.setId(model.getId());
        entity.setUserId(model.getUserId());
        entity.setCategoryId(model.getCategoryId());
        entity.setType(model.getType().name());
        entity.setAmount(model.getAmount());
        entity.setDescription(model.getDescription());
        entity.setFrequency(model.getFrequency().name());
        entity.setStartDate(model.getStartDate());
        entity.setEndDate(model.getEndDate());
        entity.setNextOccurrence(model.getNextOccurrence());
        entity.setLastGeneratedAt(model.getLastGeneratedAt());
        entity.setPaused(model.isPaused());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setUpdatedAt(model.getUpdatedAt());
        return entity;
    }

    public static RecurringTransaction toDomain(RecurringTransactionEntity entity) {
        RecurringTransaction model = new RecurringTransaction();
        model.setId(entity.getId());
        model.setUserId(entity.getUserId());
        model.setCategoryId(entity.getCategoryId());
        model.setType(TransactionType.valueOf(entity.getType()));
        model.setAmount(entity.getAmount());
        model.setDescription(entity.getDescription());
        model.setFrequency(RecurrenceFrequency.valueOf(entity.getFrequency()));
        model.setStartDate(entity.getStartDate());
        model.setEndDate(entity.getEndDate());
        model.setNextOccurrence(entity.getNextOccurrence());
        model.setLastGeneratedAt(entity.getLastGeneratedAt());
        model.setPaused(entity.isPaused());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());
        return model;
    }
}
