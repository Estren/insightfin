package com.orizon.coreapi.adapter.out.persistence;

import com.orizon.coreapi.adapter.out.persistence.mapper.CategoryPersistenceMapper;
import com.orizon.coreapi.adapter.out.persistence.repository.JpaCategoryRepository;
import com.orizon.coreapi.adapter.out.persistence.repository.JpaTransactionRepository;
import com.orizon.coreapi.domain.model.Category;
import com.orizon.coreapi.domain.model.TransactionType;
import com.orizon.coreapi.domain.port.out.CategoryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CategoryRepositoryAdapter implements CategoryRepository {

    @Inject
    JpaCategoryRepository jpaCategoryRepository;

    @Inject
    JpaTransactionRepository jpaTransactionRepository;

    @Override
    @Transactional
    public Category save(Category category) {
        var entity = CategoryPersistenceMapper.toEntity(category);
        jpaCategoryRepository.persist(entity);
        return CategoryPersistenceMapper.toDomain(entity);
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return jpaCategoryRepository.findByIdOptional(id).map(CategoryPersistenceMapper::toDomain);
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

    @Override
    public boolean hasTransactions(UUID categoryId) {
        return jpaTransactionRepository.countByCategoryId(categoryId) > 0;
    }

    @Override
    @Transactional
    public void deleteById(UUID categoryId) {
        jpaCategoryRepository.deleteById(categoryId);
    }
}
