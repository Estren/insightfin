package com.insightfin.coreapi.adapter.in.web.dto;

import com.insightfin.coreapi.domain.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCategoryRequest(
        @NotBlank String name,
        @NotNull TransactionType type,
        String icon,
        String color
) {}
