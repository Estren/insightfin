package com.insightfin.coreapi.adapter.in.web.dto;

import com.insightfin.coreapi.domain.model.AiFeedbackType;

import java.time.LocalDateTime;
import java.util.UUID;

public record AiFeedbackResponse(
        UUID id,
        AiFeedbackType type,
        String title,
        String content,
        String metadata,
        String referenceMonth,
        boolean read,
        LocalDateTime createdAt
) {}
