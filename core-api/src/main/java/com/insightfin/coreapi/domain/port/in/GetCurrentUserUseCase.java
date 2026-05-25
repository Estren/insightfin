package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.User;

import java.util.UUID;

public interface GetCurrentUserUseCase {
    User getCurrent(UUID userId);
}
