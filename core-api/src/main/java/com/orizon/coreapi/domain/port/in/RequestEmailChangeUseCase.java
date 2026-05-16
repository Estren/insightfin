package com.orizon.coreapi.domain.port.in;

import java.util.UUID;

public interface RequestEmailChangeUseCase {
    void execute(UUID userId, String newEmail);
}
