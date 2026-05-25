package com.insightfin.coreapi.domain.port.in;

import com.insightfin.coreapi.domain.model.GoogleAuthResult;

public interface AuthenticateWithGoogleUseCase {
    GoogleAuthResult authenticateWithGoogle(String idToken, String expectedNonce);
}
