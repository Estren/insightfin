package com.orizon.coreapi.adapter.in.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateBudgetRequest(
        @NotNull @Positive BigDecimal amount) {
}
