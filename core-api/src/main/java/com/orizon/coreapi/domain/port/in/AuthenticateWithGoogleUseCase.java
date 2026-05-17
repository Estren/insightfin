package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.AuthTokens;

public interface AuthenticateWithGoogleUseCase {
    AuthTokens authenticateWithGoogle(String idToken, String expectedNonce);
}
