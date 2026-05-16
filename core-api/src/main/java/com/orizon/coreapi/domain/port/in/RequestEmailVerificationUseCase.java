package com.orizon.coreapi.domain.port.in;

import java.util.UUID;

public interface RequestEmailVerificationUseCase {
    void execute(UUID userId);
}
