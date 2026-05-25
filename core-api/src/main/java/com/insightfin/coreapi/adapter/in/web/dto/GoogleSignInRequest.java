package com.insightfin.coreapi.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleSignInRequest(
        @NotBlank String credential,
        @NotBlank String nonce
) {}
