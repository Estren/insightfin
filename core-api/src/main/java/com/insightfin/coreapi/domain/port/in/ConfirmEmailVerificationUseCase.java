package com.insightfin.coreapi.domain.port.in;

public interface ConfirmEmailVerificationUseCase {
    void confirmByLink(String token);
    void confirmByPin(String email, String pin);
}
