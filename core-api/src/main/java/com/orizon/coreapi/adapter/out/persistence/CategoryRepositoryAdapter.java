package com.orizon.coreapi.adapter.out.persistence;

import com.orizon.coreapi.adapter.out.persistence.mapper.CategoryPersistenceMapper;
import com.orizon.coreapi.adapter.out.persistence.repository.JpaCategoryRepository;
import com.orizon.coreapi.domain.model.Category;
import com.orizon.coreapi.domain.model.TransactionType;
import com.orizon.coreapi.domain.port.out.CategoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final JpaCategoryRepository jpaCategoryRepository;

    public CategoryRepositoryAdapter(JpaCategoryRepository jpaCategoryRepository) {
        this.jpaCategoryRepository = jpaCategoryRepository;
    }

    @Override
    public Category save(Category category) {
        var entity = CategoryPersistenceMapper.toEntity(category);
        var saved = jpaCategoryRepository.save(entity);
        return CategoryPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return jpaCategoryRepository.findById(id).map(CategoryPersistenceMapper::toDomain);
    }

    @Override
    public List<Category> findByUserIdAndType(UUID userId, TransactionType type) {
        return jpaCategoryRepository.findByUserIdAndType(userId, type.name())
                .stream()
                .map(CategoryPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Category> findByUserId(UUID userId) {
        return jpaCategoryRepository.findByUserId(userId)
                .stream()
                .map(CategoryPersistenceMapper::toDomain)
                .toList();
    }
}
