package com.insightfin.coreapi.domain.port.out;

import com.insightfin.coreapi.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleSub(String googleSub);
    boolean existsByEmail(String email);
    void deleteById(UUID id);
    List<User> findAll();
}
