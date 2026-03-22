package com.orizon.coreapi.adapter.out.persistence.mapper;

import com.orizon.coreapi.adapter.out.persistence.entity.UserEntity;
import com.orizon.coreapi.domain.model.User;

public class UserPersistenceMapper {

    private UserPersistenceMapper() {}

    public static UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setName(user.getName());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        return entity;
    }

    public static User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
