package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.UnreadCounts;

import java.util.UUID;

public interface GetUnreadCountsUseCase {
    UnreadCounts count(UUID userId);
}
