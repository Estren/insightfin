package com.insightfin.coreapi.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

/**
 * A Coach-proposed write action the user confirmed in the UI.
 *
 * <p>{@code action} is a whitelisted action key (e.g. {@code "create_budget"});
 * {@code params} carries the action-specific arguments. The acting user is
 * never part of this payload — it comes from the JWT.</p>
 */
public record CoachActionRequest(
        @NotBlank String action,
        Map<String, Object> params
) {}
