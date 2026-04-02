package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.AiFeedback;

import java.util.List;
import java.util.UUID;

public interface ListAiFeedbacksUseCase {
    List<AiFeedback> execute(UUID userId, String referenceMonth);
}
