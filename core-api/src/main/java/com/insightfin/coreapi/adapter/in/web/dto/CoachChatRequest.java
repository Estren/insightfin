package com.insightfin.coreapi.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CoachChatRequest(
        @NotBlank @Size(min = 1, max = 1000) String question
) {}
