package com.orizon.coreapi.adapter.out.persistence.repository;

import com.orizon.coreapi.adapter.out.persistence.entity.UserEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class JpaUserRepository implements PanacheRepositoryBase<UserEntity, UUID> {

    public Optional<UserEntity> findByEmail(String email) {
        return find("lower(email) = ?1", normalize(email)).firstResultOptional();
    }

    public Optional<UserEntity> findByGoogleSub(String googleSub) {
        return find("googleSub", googleSub).firstResultOptional();
    }

    public boolean existsByEmail(String email) {
        return count("lower(email) = ?1", normalize(email)) > 0;
    }

    private static String normalize(String email) {
        return email == null ? null : email.toLowerCase(Locale.ROOT);
    }
}
