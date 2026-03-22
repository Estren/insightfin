package com.orizon.coreapi.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateGoalContributionRequest(
        @NotNull @Positive BigDecimal amount,
        @NotNull LocalDate date
) {}
