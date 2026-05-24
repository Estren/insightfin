package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.application.pagination.Page;
import com.orizon.coreapi.application.pagination.PaginationParams;
import com.orizon.coreapi.domain.model.Notification;

import java.util.UUID;

public interface ListNotificationsUseCase {
    Page<Notification> list(UUID userId, PaginationParams params);
}
