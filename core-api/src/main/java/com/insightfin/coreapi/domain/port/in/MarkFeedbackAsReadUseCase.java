package com.insightfin.coreapi.domain.port.in;

import java.util.UUID;

public interface MarkFeedbackAsReadUseCase {
    void execute(UUID userId, UUID feedbackId, boolean markAsRead);
}
