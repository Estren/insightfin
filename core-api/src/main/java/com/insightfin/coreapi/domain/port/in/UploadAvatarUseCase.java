package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.User;

import java.util.UUID;

public interface UploadAvatarUseCase {
    User uploadAvatar(UUID userId, String fileName, byte[] data, String contentType);
}
