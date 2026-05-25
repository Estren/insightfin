package com.insightfin.coreapi.adapter.in.web.dto;

import com.insightfin.coreapi.domain.model.AiFeedbackType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateAiFeedbackRequest(
        @NotNull UUID userId,
        @NotNull AiFeedbackType type,
        @NotBlank String title,
        @NotBlank String content,
        String metadata,
        String referenceMonth
) {}
