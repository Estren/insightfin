package com.orizon.coreapi.adapter.out.persistence.repository;

import com.orizon.coreapi.adapter.out.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaCategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    List<CategoryEntity> findByUserIdAndType(UUID userId, String type);
    List<CategoryEntity> findByUserId(UUID userId);
}
