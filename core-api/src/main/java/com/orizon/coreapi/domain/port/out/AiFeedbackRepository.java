package com.orizon.coreapi.domain.port.out;

import com.orizon.coreapi.domain.model.AiFeedback;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiFeedbackRepository {
    AiFeedback save(AiFeedback feedback);
    Optional<AiFeedback> findById(UUID id);
    List<AiFeedback> findByUserId(UUID userId);
    List<AiFeedback> findByUserIdAndReferenceMonth(UUID userId, String referenceMonth);
}
