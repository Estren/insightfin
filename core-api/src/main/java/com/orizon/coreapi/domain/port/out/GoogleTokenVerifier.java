package com.orizon.coreapi.domain.port.out;

public interface GoogleTokenVerifier {

    /**
     * Verifies a Google ID token and returns the authenticated user's profile claims.
     * Throws if the token is invalid, expired, has a wrong audience, the email is unverified,
     * or the token's nonce claim does not match {@code expectedNonce}.
     */
    GoogleUserInfo verify(String idToken, String expectedNonce);

    record GoogleUserInfo(String sub, String email, boolean emailVerified, String name) {}
}
