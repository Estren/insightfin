package com.orizon.coreapi.adapter.out.persistence;

import com.orizon.coreapi.adapter.out.persistence.mapper.AiFeedbackPersistenceMapper;
import com.orizon.coreapi.adapter.out.persistence.repository.JpaAiFeedbackRepository;
import com.orizon.coreapi.domain.model.AiFeedback;
import com.orizon.coreapi.domain.port.out.AiFeedbackRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AiFeedbackRepositoryAdapter implements AiFeedbackRepository {

    @Inject
    JpaAiFeedbackRepository jpaAiFeedbackRepository;

    @Override
    @Transactional
    public AiFeedback save(AiFeedback feedback) {
        var entity = AiFeedbackPersistenceMapper.toEntity(feedback);
        var managed = jpaAiFeedbackRepository.getEntityManager().merge(entity);
        return AiFeedbackPersistenceMapper.toDomain(managed);
    }

    @Override
    public Optional<AiFeedback> findById(UUID id) {
        return jpaAiFeedbackRepository.findByIdOptional(id).map(AiFeedbackPersistenceMapper::toDomain);
    }

    @Override
    public List<AiFeedback> findByUserId(UUID userId) {
        return jpaAiFeedbackRepository.findByUserId(userId)
                .stream()
                .map(AiFeedbackPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<AiFeedback> findByUserIdAndReferenceMonth(UUID userId, String referenceMonth) {
        return jpaAiFeedbackRepository.findByUserIdAndReferenceMonth(userId, referenceMonth)
                .stream()
                .map(AiFeedbackPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public int countUnreadByUserId(UUID userId) {
        return (int) jpaAiFeedbackRepository.countUnreadByUserId(userId);
    }
}
