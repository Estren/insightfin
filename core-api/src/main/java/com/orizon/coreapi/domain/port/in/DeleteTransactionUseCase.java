package com.orizon.coreapi.domain.port.in;

import java.util.UUID;

public interface DeleteTransactionUseCase {
    void execute(UUID userId, UUID transactionId);
}
