package com.orizon.coreapi.adapter.out.persistence;

import com.orizon.coreapi.adapter.out.persistence.mapper.BudgetPersistenceMapper;
import com.orizon.coreapi.adapter.out.persistence.repository.JpaBudgetRepository;
import com.orizon.coreapi.domain.model.Budget;
import com.orizon.coreapi.domain.port.out.BudgetRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BudgetRepositoryAdapter implements BudgetRepository {

    private final JpaBudgetRepository jpaBudgetRepository;

    public BudgetRepositoryAdapter(JpaBudgetRepository jpaBudgetRepository) {
        this.jpaBudgetRepository = jpaBudgetRepository;
    }

    @Override
    public Budget save(Budget budget) {
        var entity = BudgetPersistenceMapper.toEntity(budget);
        var saved = jpaBudgetRepository.save(entity);
        return BudgetPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Budget> findById(UUID id) {
        return jpaBudgetRepository.findById(id).map(BudgetPersistenceMapper::toDomain);
    }

    @Override
    public List<Budget> findByUserIdAndMonth(UUID userId, String month) {
        return jpaBudgetRepository.findByUserIdAndMonth(userId, month)
                .stream()
                .map(BudgetPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Budget> findByUserIdAndCategoryIdAndMonth(UUID userId, UUID categoryId, String month) {
        return jpaBudgetRepository.findByUserIdAndCategoryIdAndMonth(userId, categoryId, month)
                .map(BudgetPersistenceMapper::toDomain);
    }
}
