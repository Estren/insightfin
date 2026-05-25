package com.insightfin.coreapi.domain.port.in;

import java.util.UUID;

public interface LogoutUseCase {
    void execute(UUID userId);
}
