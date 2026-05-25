package com.insightfin.coreapi.adapter.out.persistence;

import com.insightfin.coreapi.adapter.out.persistence.mapper.BudgetPersistenceMapper;
import com.insightfin.coreapi.adapter.out.persistence.repository.JpaBudgetRepository;
import com.insightfin.coreapi.domain.model.Budget;
import com.insightfin.coreapi.domain.port.out.BudgetRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class BudgetRepositoryAdapter implements BudgetRepository {

    @Inject
    JpaBudgetRepository jpaBudgetRepository;

    @Override
    @Transactional
    public Budget save(Budget budget) {
        var entity = BudgetPersistenceMapper.toEntity(budget);
        var managed = jpaBudgetRepository.getEntityManager().merge(entity);
        return BudgetPersistenceMapper.toDomain(managed);
    }

    @Override
    public Optional<Budget> findById(UUID id) {
        return jpaBudgetRepository.findByIdOptional(id).map(BudgetPersistenceMapper::toDomain);
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

    @Override
    @Transactional
    public void deleteById(UUID id) {
        jpaBudgetRepository.deleteById(id);
    }
}
