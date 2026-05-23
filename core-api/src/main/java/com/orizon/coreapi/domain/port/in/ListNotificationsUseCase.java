package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.Notification;

import java.util.List;
import java.util.UUID;

public interface ListNotificationsUseCase {
    List<Notification> list(UUID userId);
}
