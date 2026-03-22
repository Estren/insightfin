package com.orizon.coreapi.adapter.out.persistence;

import com.orizon.coreapi.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.orizon.coreapi.adapter.out.persistence.repository.JpaUserRepository;
import com.orizon.coreapi.domain.model.User;
import com.orizon.coreapi.domain.port.out.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    public UserRepositoryAdapter(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public User save(User user) {
        var entity = UserPersistenceMapper.toEntity(user);
        var saved = jpaUserRepository.save(entity);
        return UserPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaUserRepository.findById(id).map(UserPersistenceMapper::toDomain);
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
