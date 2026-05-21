package com.orizon.coreapi.domain.model;

/**
 * Result of a Google sign-in: the issued tokens plus whether the sign-in
 * created a brand-new account (vs. logging in an existing one).
 */
public record GoogleAuthResult(AuthTokens tokens, boolean isNewUser) {}
