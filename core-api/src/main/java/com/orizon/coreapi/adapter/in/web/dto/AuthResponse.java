package com.orizon.coreapi.adapter.in.web.dto;

public record AuthResponse(String accessToken, String refreshToken, boolean isNewUser) {

    /** Convenience constructor for flows where new-account detection does not apply. */
    public AuthResponse(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, false);
    }
}
