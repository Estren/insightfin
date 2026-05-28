package com.insightfin.coreapi.adapter.out.persistence;

import com.insightfin.coreapi.adapter.out.persistence.mapper.CoachThreadPersistenceMapper;
import com.insightfin.coreapi.adapter.out.persistence.repository.JpaCoachThreadRepository;
import com.insightfin.coreapi.domain.model.CoachThread;
import com.insightfin.coreapi.domain.port.out.CoachThreadRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CoachThreadRepositoryAdapter implements CoachThreadRepository {

    @Inject
    JpaCoachThreadRepository jpaRepository;

    @Override
    @Transactional
    public CoachThread save(CoachThread thread) {
        var entity = CoachThreadPersistenceMapper.toEntity(thread);
        var managed = jpaRepository.getEntityManager().merge(entity);
        return CoachThreadPersistenceMapper.toDomain(managed);
    }

    @Override
    public Optional<CoachThread> findById(UUID id) {
        return jpaRepository.findByIdOptional(id).map(CoachThreadPersistenceMapper::toDomain);
    }

    @Override
    public List<CoachThread> findByUserIdOrderByLastMessageDesc(UUID userId) {
        return jpaRepository.findByUserIdOrderByLastMessageDesc(userId)
                .stream()
                .map(CoachThreadPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
