package com.orizon.coreapi.domain.port.in;

public interface RequestPasswordResetUseCase {
    void execute(String email);
}
