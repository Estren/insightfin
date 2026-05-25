package com.insightfin.coreapi.domain.port.in;

public interface ResendEmailVerificationUseCase {
    void execute(String email);
}
