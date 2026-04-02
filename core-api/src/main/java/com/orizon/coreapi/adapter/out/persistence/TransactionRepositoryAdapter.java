package com.orizon.coreapi.adapter.out.persistence;

import com.orizon.coreapi.adapter.out.persistence.mapper.TransactionPersistenceMapper;
import com.orizon.coreapi.adapter.out.persistence.repository.JpaTransactionRepository;
import com.orizon.coreapi.domain.model.Transaction;
import com.orizon.coreapi.domain.port.out.TransactionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TransactionRepositoryAdapter implements TransactionRepository {

    @Inject
    JpaTransactionRepository jpaTransactionRepository;

    @Override
    @Transactional
    public Transaction save(Transaction transaction) {
        var entity = TransactionPersistenceMapper.toEntity(transaction);
        jpaTransactionRepository.persist(entity);
        return TransactionPersistenceMapper.toDomain(entity);
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return jpaTransactionRepository.findByIdOptional(id).map(TransactionPersistenceMapper::toDomain);
    }

    @Override
    public List<Transaction> findByUserIdAndDateBetween(UUID userId, LocalDate startDate, LocalDate endDate) {
        return jpaTransactionRepository.findByUserIdAndDateBetween(userId, startDate, endDate)
                .stream()
                .map(TransactionPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        jpaTransactionRepository.deleteById(id);
    }
}
