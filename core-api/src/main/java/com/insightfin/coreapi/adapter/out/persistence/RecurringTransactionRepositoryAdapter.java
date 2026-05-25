package com.insightfin.coreapi.adapter.out.persistence;

import com.insightfin.coreapi.adapter.out.persistence.entity.RecurringTransactionEntity;
import com.insightfin.coreapi.adapter.out.persistence.mapper.RecurringTransactionPersistenceMapper;
import com.insightfin.coreapi.adapter.out.persistence.repository.JpaRecurringTransactionRepository;
import com.insightfin.coreapi.domain.model.RecurringTransaction;
import com.insightfin.coreapi.domain.port.out.RecurringTransactionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class RecurringTransactionRepositoryAdapter implements RecurringTransactionRepository {

    @Inject
    JpaRecurringTransactionRepository jpaRepository;

    @Override
    @Transactional
    public RecurringTransaction save(RecurringTransaction recurring) {
        Optional<RecurringTransactionEntity> existing = jpaRepository.findByIdOptional(recurring.getId());
        if (existing.isPresent()) {
            RecurringTransactionEntity managed = existing.get();
            managed.setCategoryId(recurring.getCategoryId());
            managed.setType(recurring.getType().name());
            managed.setAmount(recurring.getAmount());
            managed.setDescription(recurring.getDescription());
            managed.setFrequency(recurring.getFrequency().name());
            managed.setStartDate(recurring.getStartDate());
            managed.setEndDate(recurring.getEndDate());
            managed.setNextOccurrence(recurring.getNextOccurrence());
            managed.setLastGeneratedAt(recurring.getLastGeneratedAt());
            managed.setPaused(recurring.isPaused());
            managed.setUpdatedAt(recurring.getUpdatedAt());
            return RecurringTransactionPersistenceMapper.toDomain(managed);
        }
        var entity = RecurringTransactionPersistenceMapper.toEntity(recurring);
        jpaRepository.persist(entity);
        return RecurringTransactionPersistenceMapper.toDomain(entity);
    }

    @Override
    public Optional<RecurringTransaction> findById(UUID id) {
        return jpaRepository.findByIdOptional(id).map(RecurringTransactionPersistenceMapper::toDomain);
    }

    @Override
    public List<RecurringTransaction> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(RecurringTransactionPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<RecurringTransaction> findDueByDate(LocalDate date) {
        return jpaRepository.findDueByDate(date).stream()
                .map(RecurringTransactionPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public long countActiveByCategoryId(UUID categoryId) {
        return jpaRepository.countActiveByCategoryId(categoryId);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
