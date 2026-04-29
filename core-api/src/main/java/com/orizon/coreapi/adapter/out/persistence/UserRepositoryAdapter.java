package com.orizon.coreapi.adapter.out.persistence;

import com.orizon.coreapi.adapter.out.persistence.entity.UserEntity;
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
        Optional<UserEntity> existing = jpaUserRepository.findByIdOptional(user.getId());
        if (existing.isPresent()) {
            UserEntity managed = existing.get();
            managed.setName(user.getName());
            managed.setEmail(user.getEmail());
            managed.setPasswordHash(user.getPasswordHash());
            managed.setRole(user.getRole());
            managed.setUpdatedAt(user.getUpdatedAt());
            return UserPersistenceMapper.toDomain(managed);
        }
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

    @Override
    @Transactional
    public void deleteById(UUID id) {
        jpaUserRepository.deleteById(id);
    }
}
