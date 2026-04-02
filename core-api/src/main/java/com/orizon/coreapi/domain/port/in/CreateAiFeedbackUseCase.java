package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.AiFeedback;
import com.orizon.coreapi.domain.model.AiFeedbackType;

import java.util.UUID;

public interface CreateAiFeedbackUseCase {
    AiFeedback execute(UUID userId, AiFeedbackType type, String title, String content,
                       String metadata, String referenceMonth);
}
