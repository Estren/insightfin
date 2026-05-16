package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.User;

import java.util.UUID;

public interface UpdateUserUseCase {
    User update(UUID userId, String name);
}
