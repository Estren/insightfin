package com.insightfin.coreapi.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCoachThreadRequest(
        @NotBlank @Size(min = 1, max = 1000) String firstMessage
) {}
