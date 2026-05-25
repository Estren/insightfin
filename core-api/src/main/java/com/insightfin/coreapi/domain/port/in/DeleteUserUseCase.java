package com.insightfin.coreapi.domain.port.in;

import java.util.UUID;

public interface DeleteUserUseCase {
    void delete(UUID userId);
}
