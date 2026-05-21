package com.orizon.coreapi.domain.port.in;

import com.orizon.coreapi.domain.model.GoogleAuthResult;

public interface AuthenticateWithGoogleUseCase {
    GoogleAuthResult authenticateWithGoogle(String idToken, String expectedNonce);
}
