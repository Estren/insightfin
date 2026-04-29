package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.User;

import java.util.UUID;

public interface GetCurrentUserUseCase {
    User getCurrent(UUID userId);
}
