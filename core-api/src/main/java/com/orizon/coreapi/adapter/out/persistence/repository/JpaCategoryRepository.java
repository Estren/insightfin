package com.orizon.coreapi.adapter.out.persistence.repository;

import com.orizon.coreapi.adapter.out.persistence.entity.CategoryEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class JpaCategoryRepository implements PanacheRepositoryBase<CategoryEntity, UUID> {

    public List<CategoryEntity> findByUserIdAndType(UUID userId, String type) {
        return list("userId = ?1 and type = ?2", userId, type);
    }

    public List<CategoryEntity> findByUserId(UUID userId) {
        return list("userId", userId);
    }
}
