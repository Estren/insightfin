package com.insightfin.coreapi.domain.port.in;

public interface RequestPasswordResetUseCase {
    void execute(String email);
}
