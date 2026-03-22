package com.orizon.coreapi.adapter.out.persistence;

import com.orizon.coreapi.adapter.out.persistence.mapper.TransactionPersistenceMapper;
import com.orizon.coreapi.adapter.out.persistence.repository.JpaTransactionRepository;
import com.orizon.coreapi.domain.model.Transaction;
import com.orizon.coreapi.domain.port.out.TransactionRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final JpaTransactionRepository jpaTransactionRepository;

    public TransactionRepositoryAdapter(JpaTransactionRepository jpaTransactionRepository) {
        this.jpaTransactionRepository = jpaTransactionRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        var entity = TransactionPersistenceMapper.toEntity(transaction);
        var saved = jpaTransactionRepository.save(entity);
        return TransactionPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return jpaTransactionRepository.findById(id).map(TransactionPersistenceMapper::toDomain);
    }

    @Override
    public List<Transaction> findByUserIdAndDateBetween(UUID userId, LocalDate startDate, LocalDate endDate) {
        return jpaTransactionRepository.findByUserIdAndDateBetween(userId, startDate, endDate)
                .stream()
                .map(TransactionPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaTransactionRepository.deleteById(id);
    }
}
