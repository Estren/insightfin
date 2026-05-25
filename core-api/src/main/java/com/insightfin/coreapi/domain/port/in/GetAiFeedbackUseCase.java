package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.AiFeedback;

import java.util.UUID;

public interface GetAiFeedbackUseCase {
    AiFeedback execute(UUID userId, UUID feedbackId);
}
