package com.insightfin.coreapi.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameCoachThreadRequest(
        @NotBlank @Size(min = 1, max = 200) String title
) {}
