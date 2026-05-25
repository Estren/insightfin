package com.insightfin.coreapi.domain.port.in;

import java.util.UUID;

public interface ConfirmEmailChangeUseCase {
    void confirmByLink(UUID userId, String token);
    void confirmByPin(UUID userId, String pin);
}
