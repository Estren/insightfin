package com.orizon.coreapi.domain.port.out;

import com.orizon.coreapi.domain.model.AiFeedback;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiFeedbackRepository {
    AiFeedback save(AiFeedback feedback);
    Optional<AiFeedback> findById(UUID id);
    List<AiFeedback> findByUserId(UUID userId);
    List<AiFeedback> findByUserIdAndReferenceMonth(UUID userId, String referenceMonth);
    int countUnreadByUserId(UUID userId);

    /**
     * Keyset pagination ordered by {@code (createdAt DESC, id DESC)}.
     *
     * <p>If {@code cursorCreatedAt} is {@code null}, returns the first {@code limit}
     * rows from the top. Otherwise returns the next {@code limit} rows strictly
     * after the {@code (cursorCreatedAt, cursorId)} position.
     */
    List<AiFeedback> findPage(UUID userId, LocalDateTime cursorCreatedAt, UUID cursorId, int limit);
}
