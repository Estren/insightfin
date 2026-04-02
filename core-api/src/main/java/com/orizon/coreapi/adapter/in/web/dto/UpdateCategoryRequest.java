package com.orizon.coreapi.adapter.in.web.dto;

import com.orizon.coreapi.domain.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateCategoryRequest(
        @NotBlank String name,
        @NotNull TransactionType type,
        String icon,
        String color
) {}
