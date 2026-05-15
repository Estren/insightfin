package com.orizon.coreapi.domain.port.in;

public interface ResetPasswordUseCase {
    void execute(String token, String newPassword);
}
