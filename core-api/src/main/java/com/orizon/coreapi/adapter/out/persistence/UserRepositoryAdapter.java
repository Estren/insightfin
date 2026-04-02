package com.orizon.coreapi.adapter.out.persistence;

import com.orizon.coreapi.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.orizon.coreapi.adapter.out.persistence.repository.JpaUserRepository;
import com.orizon.coreapi.domain.model.User;
import com.orizon.coreapi.domain.port.out.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserRepositoryAdapter implements UserRepository {

    @Inject
    JpaUserRepository jpaUserRepository;

    @Override
    @Transactional
    public User save(User user) {
        var entity = UserPersistenceMapper.toEntity(user);
        jpaUserRepository.persist(entity);
        return UserPersistenceMapper.toDomain(entity);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaUserRepository.findByIdOptional(id).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }
}
