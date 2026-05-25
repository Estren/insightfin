package com.insightfin.coreapi.domain.port.in;

public interface ResetPasswordUseCase {
    void execute(String token, String newPassword);
}
