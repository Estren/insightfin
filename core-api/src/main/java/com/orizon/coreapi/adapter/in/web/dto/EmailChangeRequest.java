package com.orizon.coreapi.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailChangeRequest(
        @NotBlank @Email String newEmail
) {}
