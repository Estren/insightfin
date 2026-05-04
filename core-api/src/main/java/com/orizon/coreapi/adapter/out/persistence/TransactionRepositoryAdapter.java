package com.orizon.coreapi.adapter.out.persistence;

import com.orizon.coreapi.adapter.out.persistence.mapper.TransactionPersistenceMapper;
import com.orizon.coreapi.adapter.out.persistence.repository.JpaTransactionRepository;
import com.orizon.coreapi.domain.model.Transaction;
import com.orizon.coreapi.domain.port.out.TransactionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
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
        var managed = jpaTransactionRepository.getEntityManager().merge(entity);
        return TransactionPersistenceMapper.toDomain(managed);
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

    @Override
    public BigDecimal sumAmountByUserIdAndCategoryIdAndMonth(UUID userId, UUID categoryId, String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        return jpaTransactionRepository.sumAmountByUserIdAndCategoryIdAndDateBetween(
                userId, categoryId, startDate, endDate);
    }
}
