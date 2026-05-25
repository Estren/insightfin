package com.insightfin.coreapi.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmailChangePinConfirmRequest(
        @NotBlank @Pattern(regexp = "\\d{6}") String pin
) {}
