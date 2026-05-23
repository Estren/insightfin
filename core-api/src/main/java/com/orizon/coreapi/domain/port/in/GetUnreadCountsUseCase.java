package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.UnreadCounts;

import java.util.UUID;

public interface GetUnreadCountsUseCase {
    UnreadCounts count(UUID userId);
}
