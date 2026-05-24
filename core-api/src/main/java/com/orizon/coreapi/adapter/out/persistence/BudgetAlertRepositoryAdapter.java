package com.orizon.coreapi.adapter.out.persistence;

import com.orizon.coreapi.adapter.out.persistence.mapper.BudgetAlertPersistenceMapper;
import com.orizon.coreapi.adapter.out.persistence.repository.JpaBudgetAlertRepository;
import com.orizon.coreapi.domain.model.BudgetAlert;
import com.orizon.coreapi.domain.port.out.BudgetAlertRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class BudgetAlertRepositoryAdapter implements BudgetAlertRepository {

    @Inject
    JpaBudgetAlertRepository jpaBudgetAlertRepository;

    @Override
    @Transactional
    public BudgetAlert save(BudgetAlert alert) {
        var entity = BudgetAlertPersistenceMapper.toEntity(alert);
        var managed = jpaBudgetAlertRepository.getEntityManager().merge(entity);
        return BudgetAlertPersistenceMapper.toDomain(managed);
    }

    @Override
    public Optional<BudgetAlert> findById(UUID id) {
        return jpaBudgetAlertRepository.findByIdOptional(id)
                .map(BudgetAlertPersistenceMapper::toDomain);
    }

    @Override
    public List<BudgetAlert> findByUserId(UUID userId) {
        return jpaBudgetAlertRepository.findByUserId(userId)
                .stream()
                .map(BudgetAlertPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsForBudgetAtThreshold(UUID budgetId, int thresholdPercentage) {
        return jpaBudgetAlertRepository.existsByBudgetIdAndThreshold(budgetId, thresholdPercentage);
    }

    @Override
    public int countUnreadByUserId(UUID userId) {
        return (int) jpaBudgetAlertRepository.countUnreadByUserId(userId);
    }

    @Override
    public List<BudgetAlert> findPage(UUID userId, LocalDateTime cursorCreatedAt, UUID cursorId, int limit) {
        return jpaBudgetAlertRepository.findPage(userId, cursorCreatedAt, cursorId, limit)
                .stream()
                .map(BudgetAlertPersistenceMapper::toDomain)
                .toList();
    }
}
