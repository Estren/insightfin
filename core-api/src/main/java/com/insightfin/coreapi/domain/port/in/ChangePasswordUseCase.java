package com.insightfin.coreapi.domain.port.in;

import java.util.UUID;

public interface ChangePasswordUseCase {
    void changePassword(UUID userId, String currentPassword, String newPassword);
}
