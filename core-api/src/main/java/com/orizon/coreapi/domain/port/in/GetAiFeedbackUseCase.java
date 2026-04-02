package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.AiFeedback;

import java.util.UUID;

public interface GetAiFeedbackUseCase {
    AiFeedback execute(UUID userId, UUID feedbackId);
}
