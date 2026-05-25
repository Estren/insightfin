package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.application.pagination.Page;
import com.insightfin.coreapi.application.pagination.PaginationParams;
import com.insightfin.coreapi.domain.model.Notification;

import java.util.UUID;

public interface ListNotificationsUseCase {
    Page<Notification> list(UUID userId, PaginationParams params);
}
